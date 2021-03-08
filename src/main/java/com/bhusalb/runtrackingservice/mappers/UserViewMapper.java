package com.bhusalb.runtrackingservice.mappers;

import com.bhusalb.runtrackingservice.models.User;
import com.bhusalb.runtrackingservice.repos.UserRepository;
import com.bhusalb.runtrackingservice.views.UserView;
import org.bson.types.ObjectId;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

@Mapper (componentModel = "spring", uses = {ObjectIdMapper.class, RoleMapper.class})
public abstract class UserViewMapper {

    @Autowired
    private UserRepository userRepo;

    public abstract UserView toUserView (final User user);

    public abstract List<UserView> toUserViews (final List<User> users);

    public UserView toUserViewById (final ObjectId userId) {
        final Optional<User> user = userRepo.findById(userId);
        // If user is not present or user account is locked.
        if (!user.isPresent() || !user.get().isEnabled()) {
            return null;
        }
        return toUserView(user.get());
    }
}
