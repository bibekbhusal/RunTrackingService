package com.bhusalb.runtrackingservice.helper.data;

import com.bhusalb.runtrackingservice.services.RunService;
import com.bhusalb.runtrackingservice.services.UserService;
import com.bhusalb.runtrackingservice.views.Coordinates;
import com.bhusalb.runtrackingservice.views.CreateRunRequest;
import com.bhusalb.runtrackingservice.views.RunView;
import com.bhusalb.runtrackingservice.views.UserView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class RunTestDataHelper {

    @Autowired
    private RunService runService;
    @Autowired
    private UserService userService;

    public RunView create(final String email, final double distance, final int duration, final LocalDateTime start,
                           final Coordinates coordinates) {
        final UserView userView = userService.getByEmail(email);

        final CreateRunRequest request = new CreateRunRequest();
        request.setOwnerId(userView.getId());
        request.setDistance(distance);
        request.setDuration(duration);
        request.setStartDate(start);
        request.setCoordinates(coordinates);

        return runService.create(request);
    }
}
