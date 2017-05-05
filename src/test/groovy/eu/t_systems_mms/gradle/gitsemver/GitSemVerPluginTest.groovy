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

package eu.t_systems_mms.gradle.gitsemver

import org.eclipse.jgit.api.Git
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

/**
 * @author Jan Dittberner
 */
class GitSemVerPluginTest extends Specification {
    @Rule
    TemporaryFolder temporaryFolder = new TemporaryFolder()

    File projectDir
    File buildFile
    File dirtyContentFile
    File gitIgnoreFile

    def 'exception when project root does not have a git repo'() {
        given:
        buildFile << '''
            plugins {
                id 'eu.t-systems-mms.git-semver'
            }
            version gitMavenVersion()
        '''.stripIndent()

        when:
        BuildResult buildResult = with('printVersion').buildAndFail()

        then:
        buildResult.output.contains('> Cannot find \'.git\' directory')
    }

    def 'git describe when annotated tag is present' () {
        given:
        buildFile << '''
            plugins {
                id 'eu.t-systems-mms.git-semver'
            }
            version gitMavenVersion()
        '''.stripIndent()
        gitIgnoreFile << 'build/'
        Git git = Git.init().setDirectory(projectDir).call()
        git.add().addFilepattern('.').call()
        git.commit().setMessage('initial commit').call()
        git.tag().setAnnotated(true).setMessage('1.0.0').setName('1.0.0').call()

        when:
        BuildResult buildResult = with('printVersion').build()

        then:
        buildResult.output.contains(":printVersion\n1.0.0\n")
    }

    def 'git describe when no tag is present' () {
        given:
        buildFile << '''
            plugins {
                id 'eu.t-systems-mms.git-semver'
            }
            version gitMavenVersion()
        '''.stripIndent()
        gitIgnoreFile << 'build/'
        Git git = Git.init().setDirectory(projectDir).call()
        git.add().addFilepattern('.').call()
        git.commit().setMessage('initial commit').call()

        when:
        BuildResult buildResult = with('printVersion').build()

        then:
        buildResult.output.contains(":printVersion\n0.1.0-SNAPSHOT\n")
    }

    def 'git describe when tag and commit are present' () {
        given:
        buildFile << '''
            plugins {
                id 'eu.t-systems-mms.git-semver'
            }
            version gitMavenVersion()
        '''.stripIndent()
        gitIgnoreFile << 'build/'
        Git git = Git.init().setDirectory(projectDir).call()
        git.add().addFilepattern('.').call()
        git.commit().setMessage('initial commit').call()
        git.tag().setAnnotated(true).setMessage('1.0.0').setName('1.0.0').call()
        dirtyContentFile << 'test'
        git.add().addFilepattern('.').call()
        git.commit().setMessage('second commit').call()

        when:
        BuildResult buildResult = with('printVersion').build()

        then:
        buildResult.output.contains(":printVersion\n1.1.0-SNAPSHOT\n")
    }

    private GradleRunner with(String... tasks) {
        GradleRunner.create()
                .withPluginClasspath()
                .withProjectDir(projectDir)
                .withArguments(tasks)
    }

    def setup() {
        projectDir = temporaryFolder.root
        buildFile = temporaryFolder.newFile('build.gradle')
        gitIgnoreFile = temporaryFolder.newFile('.gitignore')
        dirtyContentFile = temporaryFolder.newFile('dirty')
    }
}