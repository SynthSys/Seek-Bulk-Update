package ed.synthsys.seek.bulk.update;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import ed.synthsys.seek.client.SeekRestApiClient;
import ed.synthsys.seek.dom.assay.Assay;
import ed.synthsys.seek.dom.common.Datum;
import ed.synthsys.seek.dom.common.Permission;
import ed.synthsys.seek.dom.common.Policy;
import ed.synthsys.seek.dom.common.Resource;
import ed.synthsys.seek.dom.common.SeekRestApiError;
import ed.synthsys.seek.dom.datafile.DataFile;
import ed.synthsys.seek.dom.investigation.Investigation;
import ed.synthsys.seek.dom.modelfile.ModelFile;
import ed.synthsys.seek.dom.study.Study;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.Response;

/**
 *
 * @author Johnny Hay
 */
public class PermissionsSetter {
   
    SeekRestApiClient API_CLIENT;

    // The SEEK ID of the investigation (and its children) that the policy is being applied to

    // The SEEK ID and type of the entity that the policy role is being assigned to
    int seekRelativeId;
    String seekRelativeEntityType;
    
    ObjectMapper JSON_MAPPER = new ObjectMapper();

    public PermissionsSetter(SeekRestApiClient client) {
        this.API_CLIENT = client;
    }
    
    public PermissionsSetter(SeekRestApiClient client,
            int seekRelativeId, String seekRelativeEntityType) {
        this.API_CLIENT = client;
        this.seekRelativeId = seekRelativeId;
        this.seekRelativeEntityType = seekRelativeEntityType;
    }
    
    public static void main(String[] args) {
        int seekPersonId = 2;
        String policyAccess = "manage";
        String seekType = "person";

        SeekRestApiClient apiClient = new SeekRestApiClient("https://fairdomhub.org/");
        int investigationId = 8;

        PermissionsSetter setter = new PermissionsSetter(apiClient,
                seekPersonId, seekType);
        int exitCode = 0;

        try {
            setter.updateISAPermissions(investigationId, policyAccess);
        } catch (Exception ex) {
            exitCode = 1;
            Logger.getLogger(PermissionsSetter.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }

        // Need to exit forcefully to close Hibernate session
        System.exit(exitCode);
    }

    void updateISAPermissions(int investigationId, String newAccessPolicy)
            throws Exception {
        JSON_MAPPER.configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, true);
        
        // Create project-level permissions
        Permission permission = new Permission();
        permission.setAccess(newAccessPolicy);
        
        Resource resource = new Resource();
        resource.setType(seekRelativeEntityType);
        resource.setId(String.valueOf(seekRelativeId));   
        permission.setResource(resource);
        
        setInvestigationPermissions(investigationId,permission,true);
    }

    void setInvestigationPermissions(int investigationId, Permission permission, boolean recursive) throws Exception {
        Investigation investigation = API_CLIENT.getInvestigation(investigationId);

        updateInvestigationPermissions(investigationId, investigation, permission);
        
        if (recursive){
            List<Integer> studies = getStudiesForInvestigation(investigationId);
            for (Integer study: studies) {
                updateStudyPermissions(study, permission, recursive);
            }
        }
    }

    void updateInvestigationPermissions(int investigationId, Investigation investigation, Permission permission) throws Exception {
        Policy curPolicy = investigation.getData().getAttributes().getPolicy();
        curPolicy = updatePolicy(curPolicy, permission);
        
        investigation.getData().getAttributes().setPolicy(curPolicy);

        Response response = API_CLIENT.updateInvestigation(String.valueOf(investigationId), investigation);
        if(response.getStatus() != 200) {
            Exception ex = this.getException(response);

            throw(ex);
        }
    }

    List<Integer> getStudiesForInvestigation(int investigationId) {
        Investigation investigation = API_CLIENT.getInvestigation(investigationId);
        
        List<Datum> studyData = investigation.getData().getRelationships().getStudies().getData();
        List<Integer> studyIds = new ArrayList();
        
        for(Datum studyDatem: studyData) {
            studyIds.add(Integer.parseInt(studyDatem.getId()));
        }
        
        return studyIds;
    }

    void updateStudyPermissions(Integer studyId, Permission permission, boolean recursive) throws Exception {
        Study study = API_CLIENT.getStudy(studyId);
        Policy curPolicy = study.getData().getAttributes().getPolicy();
        curPolicy = updatePolicy(curPolicy, permission);
        
        study.getData().getAttributes().setPolicy(curPolicy);

        Response response = API_CLIENT.updateStudy(String.valueOf(studyId), study);
        if(response.getStatus() != 200) {
            Exception ex = this.getException(response);

            throw(ex);
        }

        if (recursive){
            List<Integer> assays = getAssaysForStudy(studyId);
            for (Integer assay: assays) {
                updateAssayPermissions(assay, permission, recursive);
            }
        }
    }

    List<Integer> getAssaysForStudy(int studyId) {
        Study study = API_CLIENT.getStudy(studyId);
        
        List<Datum> assayData = study.getData().getRelationships().getAssays().getData();
        List<Integer> assayIds = new ArrayList();
        
        for(Datum assayDatum: assayData) {
            assayIds.add(Integer.parseInt(assayDatum.getId()));
        }
        
        return assayIds;
    }

    void updateAssayPermissions(Integer assayId, Permission permission, boolean recursive) throws Exception {
        Assay assay = API_CLIENT.getAssay(assayId);
        Policy curPolicy = assay.getData().getAttributes().getPolicy();
        curPolicy = updatePolicy(curPolicy, permission);
        
        assay.getData().getAttributes().setPolicy(curPolicy);

        Response response = API_CLIENT.updateAssay(String.valueOf(assayId), assay);
        if(response.getStatus() != 200) {
            Exception ex = this.getException(response);

            throw(ex);
        }

        if (recursive){
            List<Integer> models = getModelsForAssay(assayId);
            for (Integer model: models) {
                updateModelPermissions(model, permission, recursive);
            }

            List<Integer> dataFiles = getDataFilesForAssay(assayId);
            for (Integer dataFile: dataFiles) {
                updateDataFilePermissions(dataFile, permission, recursive);
            }
        }
    }
    
    void updateModelPermissions(Integer modelId, Permission permission, boolean recursive) throws Exception {
        ModelFile model = API_CLIENT.getModelFile(modelId);
        Policy curPolicy = model.getData().getAttributes().getPolicy();
        curPolicy = updatePolicy(curPolicy, permission);
        
        model.getData().getAttributes().setPolicy(curPolicy);

        Response response = API_CLIENT.updateModelFile(String.valueOf(modelId), model);
        if(response.getStatus() != 200) {
            Exception ex = this.getException(response);

            throw(ex);
        }
    }

    List<Integer> getModelsForAssay(int assayId) {
        Assay assay = API_CLIENT.getAssay(assayId);
        
        List<Datum> modelData = assay.getData().getRelationships().getModels().getData();
        List<Integer> modelIds = new ArrayList();
        
        for(Datum modelDatum: modelData) {
            modelIds.add(Integer.parseInt(modelDatum.getId()));
        }
        
        return modelIds;
    }

    void updateDataFilePermissions(Integer dataFileId, Permission permission, boolean b) throws Exception {
        DataFile dataFile = API_CLIENT.getDataFile(dataFileId);
        Policy curPolicy = dataFile.getData().getAttributes().getPolicy();
        curPolicy = updatePolicy(curPolicy, permission);
        
        dataFile.getData().getAttributes().setPolicy(curPolicy);

        Response response = API_CLIENT.updateDataFile(String.valueOf(dataFileId), dataFile);
        if(response.getStatus() != 200) {
            Exception ex = this.getException(response);

            throw(ex);
        }
    }

    List<Integer> getDataFilesForAssay(int assayId) {
        Assay assay = API_CLIENT.getAssay(assayId);
        
        List<Datum> dataFileData = assay.getData().getRelationships().getDataFiles().getData();
        List<Integer> dataFileIds = new ArrayList();
        
        for(Datum dataFileDatum: dataFileData) {
            dataFileIds.add(Integer.parseInt(dataFileDatum.getId()));
        }
        
        return dataFileIds;
    }

    private Policy updatePolicy(Policy policy, Permission permission) {
        // Check if permission for this entity exists already
        List<Permission> permissions = policy.getPermissions();
        
        for(Permission nextPerm: permissions) {
            Resource resource = nextPerm.getResource();
            if (resource.getType().equals(seekRelativeEntityType) && 
                    Integer.parseInt(resource.getId()) == seekRelativeId) {
                System.out.println("Exists, setting current policy permission!");
                nextPerm.setAccess(permission.getAccess());
                return policy;
            }
        }
        
        permissions.add(permission);
        policy.setPermissions(permissions);
        return policy;
    }
    
    private Exception getException(Response response) throws IOException {
        String errResStr = response.readEntity(String.class);

        Map<String, List<SeekRestApiError>> errResMap = JSON_MAPPER.readValue(errResStr,
            new TypeReference<Map<String,List<SeekRestApiError>>>(){});

        List<SeekRestApiError> errors = new ArrayList();
        List<String> errMsgs = new ArrayList();

        Iterator it = errResMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            List<SeekRestApiError> errsList = (List<SeekRestApiError>)pair.getValue();

            errors.addAll(errsList);
        }

        for (SeekRestApiError error: errors) {               
            errMsgs.add(error.getDetail());
        }

        String exMessage = String.join("; ", errMsgs);
        
        Exception ex = new Exception(exMessage);
        
        return ex;
    }
}
