package com.bhusalb.runtrackingservice.controllers;


import com.bhusalb.runtrackingservice.mappers.ObjectIdMapper;
import com.bhusalb.runtrackingservice.models.Roles;
import com.bhusalb.runtrackingservice.models.User;
import com.bhusalb.runtrackingservice.services.RunService;
import com.bhusalb.runtrackingservice.views.CreateRunRequest;
import com.bhusalb.runtrackingservice.views.ListResponse;
import com.bhusalb.runtrackingservice.views.RunView;
import com.bhusalb.runtrackingservice.views.SearchRequest;
import com.bhusalb.runtrackingservice.views.SearchRunQuery;
import com.bhusalb.runtrackingservice.views.UpdateRunRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

@Tag (name = "Run")
@RestController
@RequiredArgsConstructor
@RequestMapping ("/v1/runs/")
@Slf4j
@Validated
public class RunController {

    private final RunService runService;
    private final ObjectIdMapper objectIdMapper;

    @PreAuthorize ("hasRole('ADMIN') || (@objectIdMapperImpl.stringToObjectId(#request.ownerId) == principal.id)")
    @PostMapping
    public RunView create (@RequestBody @Valid CreateRunRequest request) {
        return runService.create(request);
    }

    @PutMapping ("{runId}")
    public RunView update (@PathVariable @NotBlank String runId, @RequestBody @Valid UpdateRunRequest request,
                           final Authentication authentication) {
        final User user = (User) authentication.getPrincipal();
        final ObjectId runObjectId = objectIdMapper.stringToObjectId(runId);
        final RunView runView = runService.getRunById(runObjectId);

        // If user is an Admin or a record owner, then allow to update.
        if (user.getRoles().contains(Roles.ADMIN) || user.getId().toString().equals(runView.getOwner().getId())) {
            return runService.update(runObjectId, request);
        }
        throw new HttpClientErrorException(HttpStatus.FORBIDDEN, "Not authorized to update the given run.");
    }

    @DeleteMapping ("{runId}")
    public RunView delete (@PathVariable @NotBlank String runId, final Authentication authentication) {
        final ObjectId runObjectId = objectIdMapper.stringToObjectId(runId);
        final RunView runView = runService.getRunById(runObjectId);
        final User user = (User) authentication.getPrincipal();

        // If user is an Admin or a record owner, then allow to update.
        if (user.getRoles().contains(Roles.ADMIN) || user.getId().toString().equals(runView.getOwner().getId())) {
            return runService.delete(runObjectId);
        }
        throw new HttpClientErrorException(HttpStatus.FORBIDDEN, "Not authorized to delete the given run.");
    }

    @GetMapping ("{runId}")
    public RunView get (@PathVariable @NotBlank String runId, final Authentication authentication) {
        final ObjectId runObjectId = objectIdMapper.stringToObjectId(runId);
        final RunView runView = runService.getRunById(runObjectId);
        final User user = (User) authentication.getPrincipal();

        // If user is an Admin or a record owner, then allow to update.
        if (user.getRoles().contains(Roles.ADMIN) || user.getId().toString().equals(runView.getOwner().getId())) {
            return runView;
        }
        throw new HttpClientErrorException(HttpStatus.FORBIDDEN, "Not authorized to GET the given run.");
    }

    @PreAuthorize ("hasRole('ADMIN') || (#request.ownerId == principal.username)")
    @PostMapping ("/v1/runs/search")
    public ListResponse<RunView> search (@RequestBody SearchRequest<SearchRunQuery> request) {
        return null;
    }
}
