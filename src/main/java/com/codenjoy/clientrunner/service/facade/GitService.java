package com.codenjoy.clientrunner.service.facade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GitService {

    public Optional<Git> clone(String repoURI, File directory) {
        try {
            return Optional.ofNullable(Git.cloneRepository()
                    .setURI(repoURI)
                    .setDirectory(directory)
                    .call());
        } catch (GitAPIException e) {
            log.error("Can not clone repository: {}", repoURI);
            return Optional.empty();
        }
    }
}
