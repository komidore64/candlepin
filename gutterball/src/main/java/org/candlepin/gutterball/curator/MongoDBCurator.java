/**
 * Copyright (c) 2009 - 2012 Red Hat, Inc.
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

package org.candlepin.gutterball.curator;
/**
 * Copyright (c) 2009 - 2012 Red Hat, Inc.
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

import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
 *
 * An abstract base class for gutterball curators. A curator is a wrapper
 * around a mongodb collection. DBObjects returned by the mongo java driver
 * can be safely casted to M.
 *
 * @param <M> a DBOject class representing the data stored in the collection.
 */
public abstract class MongoDBCurator<M extends DBObject> {

    private DBCollection collection;

    public MongoDBCurator(Class<M> modelClass, DB database) {
        this.collection = database.getCollection(getCollectionName());
        this.collection.setObjectClass(modelClass);
    }

    /**
     * Defines the name of the collection to which this curator uses.
     * @return the name of the collection.
     */
    public abstract String getCollectionName();

    public DBCursor all() {
        return collection.find();
    }

    public void insert(DBObject toInsert) {
        collection.insert(toInsert);
    }

    public void save(DBObject toSave) {
        collection.save(toSave);
    }

    public M findById(String id) {
        return (M) collection.findOne(new BasicDBObject("_id", new ObjectId(id)));
    }

    public long count() {
        return collection.count();
    }

}
