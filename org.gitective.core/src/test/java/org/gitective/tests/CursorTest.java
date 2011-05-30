/******************************************************************************
 *  Copyright (c) 2011, Kevin Sawicki <kevinsawicki@gmail.com>
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *****************************************************************************/
package org.gitective.tests;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepository;
import org.gitective.core.CommitUtils;
import org.gitective.core.filter.commit.AndCommitFilter;
import org.gitective.core.filter.commit.CommitCursorFilter;
import org.gitective.core.filter.commit.CommitLimitFilter;
import org.gitective.core.filter.commit.CommitListFilter;
import org.gitective.core.service.CommitFinder;

/**
 * Unit tests of {@link CommitCursorFilter}
 */
public class CursorTest extends GitTestCase {

	/**
	 * Test traversing commits in chunks
	 * 
	 * @throws Exception
	 */
	public void testChunk() throws Exception {
		int commitCount = 50;
		final List<RevCommit> commits = new ArrayList<RevCommit>(commitCount);
		for (int i = 0; i < commitCount; i++)
			commits.add(add("file.txt", "revision " + i));

		CommitFinder service = new CommitFinder(testRepo);
		CommitListFilter bucket = new CommitListFilter();
		CommitLimitFilter limit = new CommitLimitFilter(10);
		limit.setStop(true);

		CommitCursorFilter cursor = new CommitCursorFilter(new AndCommitFilter(
				limit, bucket));
		service.setRevFilter(cursor);
		int chunks = 0;
		RevCommit commit = CommitUtils.getLatest(new FileRepository(testRepo));
		while (commit != null) {
			service.findFrom(commit);
			assertEquals(limit.getLimit(), bucket.getCommits().size());
			commits.removeAll(bucket.getCommits());
			commit = cursor.getLast();
			cursor.reset();
			chunks++;
		}
		assertEquals(commitCount / limit.getLimit(), chunks);
		assertTrue(commits.isEmpty());
	}
}
