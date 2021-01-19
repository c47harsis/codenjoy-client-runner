package com.codenjoy.clientrunner.service;

import lombok.AllArgsConstructor;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
@AllArgsConstructor
public class GitService {

    public Git clone(String repoURI, File directory) {
        Git clonedRepo = null;
        try {
            clonedRepo = Git.cloneRepository()
                    .setURI(repoURI)
                    .setDirectory(directory)
                    .call();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
        return clonedRepo;
    }
}
