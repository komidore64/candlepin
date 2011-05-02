/**
 * Copyright (c) 2009 Red Hat, Inc.
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package org.fedoraproject.candlepin.resource.test;

import static org.junit.Assert.assertEquals;

import org.fedoraproject.candlepin.model.Owner;
import org.fedoraproject.candlepin.model.User;
import org.fedoraproject.candlepin.resource.OwnerResource;
import org.fedoraproject.candlepin.resource.UserResource;
import org.fedoraproject.candlepin.test.DatabaseTestFixture;
import org.junit.Before;
import org.junit.Test;

/**
 * UserResourceTest
 */
public class UserResourceTest extends DatabaseTestFixture {
    
    private UserResource userResource;
    private OwnerResource ownerResource;
    private Owner owner;


    
    @Before
    public void setUp() {
        owner = createOwner();       
        userResource = injector.getInstance(UserResource.class);
        ownerResource = injector.getInstance(OwnerResource.class);
    }
    
    @Test
    public void testLookupUser() {

        User user = new User();
        user.setUsername("henri");
        user.setPassword("password");

        String ownerKey = owner.getKey();
        ownerResource.createUser(ownerKey, user);

        User u = userResource.getUserInfo("henri");

        assertEquals(user.getId(), u.getId());
    }

}