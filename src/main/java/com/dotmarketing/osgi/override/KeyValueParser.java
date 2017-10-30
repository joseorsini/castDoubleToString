package com.dotmarketing.osgi.override;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dotcms.repackage.org.apache.commons.lang.math.NumberUtils;

import com.dotcms.content.elasticsearch.constants.ESMappingConstants;
import com.dotcms.contenttype.model.field.FieldType;
import com.dotcms.contenttype.model.field.FieldTypeAPI;
import com.dotcms.contenttype.model.field.KeyValueField;
import com.dotcms.contenttype.model.type.ContentType;
import com.dotcms.contenttype.transform.field.LegacyFieldTransformer;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.exception.DotSecurityException;
import com.dotmarketing.portlets.contentlet.model.Contentlet;
import com.dotmarketing.portlets.structure.model.Field;
import com.dotmarketing.util.Logger;
import com.dotmarketing.util.json.JSONException;
import com.dotmarketing.util.json.JSONObject;
import com.liferay.portal.model.User;

public class KeyValueParser {

    public String RESEARCH_CONTENT_VARIABLE = "Researchcontent";
    public User systemUser = APILocator.systemUser();

    public void keyValueFieldsHandler () {
        try {
            ContentType researchContentContentType = APILocator.getContentTypeAPI(systemUser)
                    .find(RESEARCH_CONTENT_VARIABLE);
            Logger.info(this,"ContentType: " + researchContentContentType.name());
            List<Contentlet> cons = APILocator.getContentletAPI().
                    findByStructure(researchContentContentType.inode(),
                            systemUser, false, 0, 0);

            for(Contentlet con : cons) {
                List<Field> fields = new LegacyFieldTransformer(
                        APILocator.getContentTypeAPI(APILocator.systemUser()).
                        find(researchContentContentType.inode()).fields()).asOldFieldList();

                int fieldsUpdated = 0;
                Contentlet newCon = con;
                for(Field field : fields){
                    if (field.getFieldType().equals(ESMappingConstants.FIELD_TYPE_KEY_VALUE)){

                        Map<String,Object> keyValueMap = con.getKeyValueProperty(field.getVelocityVarName());
                        Map<String,Object> newKeyValueMap = new HashMap<String,Object>();
                        if(keyValueMap!=null && !keyValueMap.isEmpty()){
                            for(Map.Entry<String, Object> entry : keyValueMap.entrySet()){
                                
                                String keyObj = entry.getKey();
                                Object valueObj = entry.getValue();
                                Logger.info(this,"Key: " + entry.getKey());
                                Logger.info(this,"Value: " + entry.getValue());

                                try{
                                    if(NumberUtils.isNumber(valueObj.toString())){
                                        int tempPlaceHolder = (int)Double.parseDouble((String)valueObj);
                                        newKeyValueMap.put(keyObj, tempPlaceHolder);
                                    } else {
                                        newKeyValueMap.put(keyObj, valueObj);
                                    }
                                }catch (Exception e){
                                    Logger.error(this, "What?: " + e.getMessage(), e);
                                    newKeyValueMap.put(keyObj, valueObj.toString());
                                }
                            }
                        }
                        
                        JSONObject jsonObj = new JSONObject();
                        for(Map.Entry<String, Object> entry : newKeyValueMap.entrySet()){
                            String keyObj = entry.getKey();
                            Object valueObj = entry.getValue();
                            try {
                                jsonObj.put(keyObj, valueObj.toString());
                            } catch (Exception e) {
                                Logger.error(this, "Error: " + e.getMessage());
                            }
                        }
                        newCon.setStringProperty(field.getVelocityVarName(), jsonObj.toString());
                    }
                }
                newCon.setInode("");
                Logger.info(this,"Updating content with Id: " + con.getIdentifier());
                User modUser = APILocator.getUserAPI().loadUserById(con.getModUser());
                APILocator.getContentletAPI().checkin(newCon,modUser,false);
                //APILocator.getContentletAPI().publish(newCon,modUser,false);
                //APILocator.getContentletAPI().unlock(newCon,modUser,false);
            }            
        } catch (DotDataException e) {
            Logger.error(this, "Error: " + e.getMessage());
        } catch (DotSecurityException e) {
            Logger.error(this, "Error: " + e.getMessage());
        } 
    }
}
