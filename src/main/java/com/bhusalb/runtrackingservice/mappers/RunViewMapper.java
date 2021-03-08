package com.bhusalb.runtrackingservice.mappers;

import com.bhusalb.runtrackingservice.models.Run;
import com.bhusalb.runtrackingservice.views.Coordinates;
import com.bhusalb.runtrackingservice.views.RunView;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper (componentModel = "spring", uses = {ObjectIdMapper.class})
public abstract class RunViewMapper {
    @Autowired
    private UserViewMapper userViewMapper;

    public abstract RunView toRunView (final Run run);

    public abstract List<RunView> toRunViews (final List<Run> runs);

    @AfterMapping
    protected void after (final Run run, @MappingTarget final RunView runView) {
        runView.setOwner(userViewMapper.toUserViewById(run.getOwnerId()));
        runView.setCoordinates(Coordinates.fromGeoJSONPoint(run.getLocation()));
    }
}
