/*
 * Copyright (c) 2017 T-Systems Multimedia Solutions GmbH.
 * Riesaer Str. 5, D-01129 Dresden, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 *
 */
package eu.t_systems_mms.gradle.gitsemver

import groovy.transform.Memoized
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.gradle.api.Plugin
import org.gradle.api.Project
/**
 * @author Jan Dittberner
 */
class GitSemVerPlugin implements Plugin<Project> {
    @Memoized
    private static File gitDir(Project project) {
        return getRootGitDir(project.rootDir)
    }

    @Memoized
    private static Git gitRepo(Project project) {
        return Git.wrap(new FileRepository(gitDir(project)))
    }

    private static String gitDesc(Project project) {
        Git git = gitRepo(project)
        def gitVersion = git.describe().call()

        if (gitVersion ==~ /^\d+\.\d+\.\d+$/) {
            return gitVersion
        }
        if (gitVersion ==~ /^\d+\.\d+\.\d+.*/) {
            String lastTag, distance, shortHash
            String major, minor, fix
            (lastTag, distance, shortHash) = gitVersion.split('-', 3)
            project.logger.debug(
                    "found last tag {}, distance {}, short hash {} in git version",
                    lastTag, distance, shortHash)

            (major, minor, fix) = lastTag.split("\\.", 3)

            project.logger.debug(
                    "found major {}, minor {}, fix {} version in last tag", major, minor, fix)

            return String.format("%s.%d.0-SNAPSHOT", major, Integer.parseInt(minor) + 1)
        }
        return "0.1.0-SNAPSHOT"
    }

    @Override
    void apply(Project project) {
        project.ext.gitMavenVersion = {
            return gitDesc(project)
        }

        project.tasks.create('printVersion') {
            group = 'Versioning'
            description = 'Prints the project\'s configured version to standard out'
            doLast {
                println project.version
            }
        }
    }

    private static File getRootGitDir(File currentRoot) {
        File gitDir = scanForRootGitDir(currentRoot)
        if (!gitDir.exists()) {
            throw new IllegalArgumentException('Cannot find \'.git\' directory')
        }
        return gitDir
    }

    private static File scanForRootGitDir(File currentRoot) {
        File gitDir = new File(currentRoot, '.git')

        if (gitDir.exists()) {
            return gitDir
        }

        // stop at the root directory, return non-existing File object
        if (currentRoot.parentFile == null) {
            return gitDir
        }

        // look in parent directory
        return scanForRootGitDir(currentRoot.parentFile)
    }
}
