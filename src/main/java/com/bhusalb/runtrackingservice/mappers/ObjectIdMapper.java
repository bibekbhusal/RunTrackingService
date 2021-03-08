package com.bhusalb.runtrackingservice.mappers;

import org.bson.types.ObjectId;
import org.mapstruct.Mapper;

@Mapper (componentModel = "spring")
public abstract class ObjectIdMapper {

    public String objectIdToString (final ObjectId objectId) {
        return objectId.toString();
    }

    public ObjectId stringToObjectId (final String string) {
        return new ObjectId(string);
    }
}
