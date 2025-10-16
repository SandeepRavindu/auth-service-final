package com.creatorboost.auth_service.io;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientProfileRequset {

    private String location;
    private String preferences;
    private String description;
}
