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
package org.fedoraproject.candlepin.auth;

import org.fedoraproject.candlepin.auth.permissions.Permission;
import org.fedoraproject.candlepin.util.Util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * An entity interacting with Candlepin
 */
public abstract class Principal implements Serializable {

    protected List<Permission> permissions = new ArrayList<Permission>();

    public abstract String getType();

    public abstract boolean hasFullAccess();

    protected void addPermission(Permission permission) {
        this.permissions.add(permission);
    }

    public final boolean canAccess(Object target, Access access) {
        for (Permission permission : permissions) {
            if (permission.canAccess(target, access)) {
                // if any of the principal's permissions allows access, then
                // we are good to go
                return true;
            }
        }

        // none of the permissions grants access, so this target is not allowed
        return false;
    }
    
    public String getPrincipalName() {
        return "";
    }
    
    public PrincipalData getData() {
        return new PrincipalData(this.getType(), this.getPrincipalName());
    }

    @Override
    public String toString() {
        return Util.toJson(this.getData());
    }

}
