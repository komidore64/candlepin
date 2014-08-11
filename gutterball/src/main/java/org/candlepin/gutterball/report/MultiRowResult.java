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

package org.candlepin.gutterball.report;

import java.util.LinkedList;
import java.util.List;

/**
 * A {@link ReportResult} implementation that stores its results as a list of rows.
 *
 * @param <R> an object representing a row of data.
 */
public class MultiRowResult<R> implements ReportResult {
    private List<R> results;

    public MultiRowResult() {
        this.results = new LinkedList<R>();
    }

    public void addRow(R resultRow) {
        this.results.add(resultRow);
    }

    public List<R> getRows() {
        return this.results;
    }
}
