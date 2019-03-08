/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.synthsys.seek.bulk.update;

import ed.synthsys.seek.client.SeekRestApiClient;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

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

    static int investigationId = 1; // tzielins

    // The SEEK ID of the entity that the policy role is being assigned to
    static int seekRelativeId = 2; // tzielins
    static String seekRelativeEntityType = "people";
    static String policyAccess = "manage";
    
    static PermissionsSetter setter;
    
    @BeforeClass
    public static void setUpClass() {

    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        SeekRestApiClient apiClient = new SeekRestApiClient(seekURI,
            userName, password);
        setter = new PermissionsSetter(apiClient,
                seekRelativeId, seekRelativeEntityType);
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void testUpdateISAPermissions() {
        int exitCode = 0;
        
        try {
            setter.updateISAPermissions(investigationId, policyAccess);
        } catch (Exception ex) {
            exitCode = 1;
            Logger.getLogger(PermissionsSetter.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        
        assertEquals(0, exitCode);
    }
}
