/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.synthsys.seek.bulk.update;

import ed.synthsys.seek.client.SeekRestApiClient;
import ed.synthsys.seek.dom.common.Permission;
import ed.synthsys.seek.dom.common.Policy;
import ed.synthsys.seek.dom.common.Resource;
import ed.synthsys.seek.dom.investigation.Investigation;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author jhay
 */
public class PermissionsSetterTest {
    
    public PermissionsSetterTest() {
    }
        
    static String seekURI = "https://fairdomhub.org/";
    
    // don't commit real username and password
    static String userName = "test";
    static String password = "test";

    static int investigationId = 8; // tzielins

    // The SEEK ID of the entity that the policy role is being assigned to
    static int seekRelativeId = 2; // tzielins
    static String seekRelativeEntityType = "people";
    static String policyAccess = "manage";

    static SeekRestApiClient apiClient;
    static PermissionsSetter setter;

    @BeforeClass
    public static void setUpClass() {

    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        apiClient = new SeekRestApiClient(seekURI, userName, password);
        //apiClient = new SeekRestApiClient(seekURI);
        setter = new PermissionsSetter(apiClient,
                seekRelativeId, seekRelativeEntityType);
    }
    
    @After
    public void tearDown() {
        apiClient.close();
    }
    
    @Test
    public void setUpWorks() {
        assertNotNull(apiClient);
        assertNotNull(setter);
        
        Investigation i = apiClient.getInvestigation(investigationId);
        assertNotNull(i);
    }

    @Test
    public void testUpdateISAPermissions() throws Exception {

        setter.updateISAPermissions(investigationId, policyAccess);

        // Verify update was successful
        Investigation inv = apiClient.getInvestigation(investigationId);
        Policy policy = inv.getData().getAttributes().getPolicy();
        assertTrue(verifyPolicyPermissions(policy));
    }

    private boolean verifyPolicyPermissions(Policy policy) {
        List<Permission> permissions = policy.getPermissions();

        for(Permission nextPerm: permissions) {
            Resource resource = nextPerm.getResource();
            if (resource.getType().equals(seekRelativeEntityType) &&
                    Integer.parseInt(resource.getId()) == seekRelativeId) {

                if (nextPerm.getAccess().equals(policyAccess)) {
                    return true;
                }
            }
        }

        return false;
    }
}
