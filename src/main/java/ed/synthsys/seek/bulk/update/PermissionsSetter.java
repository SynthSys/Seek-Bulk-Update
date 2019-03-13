package ed.synthsys.seek.bulk.update;

import ed.synthsys.seek.client.SeekRestApiClient;
import ed.synthsys.seek.dom.assay.Assay;
import ed.synthsys.seek.dom.common.Datum;
import ed.synthsys.seek.dom.common.Permission;
import ed.synthsys.seek.dom.common.Policy;
import ed.synthsys.seek.dom.common.Resource;
import ed.synthsys.seek.dom.datafile.DataFile;
import ed.synthsys.seek.dom.investigation.Investigation;
import ed.synthsys.seek.dom.modelfile.ModelFile;
import ed.synthsys.seek.dom.study.Study;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Johnny Hay
 */
public class PermissionsSetter implements AutoCloseable {
   
    final SeekRestApiClient API_CLIENT;

    // The SEEK ID and type of the entity that the policy role is being assigned to
    int seekRelativeId;
    String seekRelativeEntityType;
    

    public PermissionsSetter(SeekRestApiClient client) {
        this.API_CLIENT = client;
    }
    
    public PermissionsSetter(SeekRestApiClient client, int seekRelativeId, String seekRelativeEntityType) {
        this(client);
        this.seekRelativeId = seekRelativeId;
        this.seekRelativeEntityType = seekRelativeEntityType;
    }
    
    public static void main(String[] args) {
        /*
        * Configuration Properties
        */
        int investigationId = 8;
        int seekPersonId = 2;
        String policyAccess = "manage";
        String seekType = "person";
        //remember the trailing /
        String url = "https://fairdomhub.org/";
        String userName = "test";
        String password = "test";

        /*
        * End of Configuration Properties
        */
        

        try (SeekRestApiClient apiClient = new SeekRestApiClient(url, userName, password)) {

            PermissionsSetter setter = new PermissionsSetter(apiClient, seekPersonId, seekType);
            
            setter.updateISAPermissions(investigationId, policyAccess);
            
        } catch (Exception ex) {
            Logger.getLogger(PermissionsSetter.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }

    }

    void updateISAPermissions(int investigationId, String newAccessPolicy)
            throws Exception {
        
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

    void updateInvestigationPermissions(int investigationId, Investigation investigation, Permission permission)  {
        Policy curPolicy = investigation.getData().getAttributes().getPolicy();
        curPolicy = updatePolicy(curPolicy, permission);
        
        investigation.getData().getAttributes().setPolicy(curPolicy);

        API_CLIENT.updateInvestigation(investigationId, investigation);
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

    void updateStudyPermissions(Integer studyId, Permission permission, boolean recursive)  {
        Study study = API_CLIENT.getStudy(studyId);
        Policy curPolicy = study.getData().getAttributes().getPolicy();
        curPolicy = updatePolicy(curPolicy, permission);
        
        study.getData().getAttributes().setPolicy(curPolicy);

        API_CLIENT.updateStudy(studyId, study);

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

    void updateAssayPermissions(Integer assayId, Permission permission, boolean recursive) {
        Assay assay = API_CLIENT.getAssay(assayId);
        Policy curPolicy = assay.getData().getAttributes().getPolicy();
        curPolicy = updatePolicy(curPolicy, permission);
        
        assay.getData().getAttributes().setPolicy(curPolicy);

        API_CLIENT.updateAssay(assayId, assay);

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
    
    void updateModelPermissions(Integer modelId, Permission permission, boolean recursive) {
        ModelFile model = API_CLIENT.getModelFile(modelId);
        Policy curPolicy = model.getData().getAttributes().getPolicy();
        curPolicy = updatePolicy(curPolicy, permission);
        
        model.getData().getAttributes().setPolicy(curPolicy);

        API_CLIENT.updateModelFile(modelId, model);
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

    void updateDataFilePermissions(Integer dataFileId, Permission permission, boolean b) {
        DataFile dataFile = API_CLIENT.getDataFile(dataFileId);
        Policy curPolicy = dataFile.getData().getAttributes().getPolicy();
        curPolicy = updatePolicy(curPolicy, permission);
        
        dataFile.getData().getAttributes().setPolicy(curPolicy);

        API_CLIENT.updateDataFile(dataFileId, dataFile);
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
        if (policy == null) {
            throw new IllegalStateException("The current policy is null, most likely user has no rights to read policy");
        }
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

    @Override
    public void close() throws Exception {
        if (API_CLIENT != null) {
            API_CLIENT.close();
        }
    }
    
}
