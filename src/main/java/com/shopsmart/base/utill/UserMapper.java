package com.shopsmart.base.utill;

import com.shopsmart.base.dto.RegisterRequest;
import com.shopsmart.base.model.Role;
import com.shopsmart.base.model.User;

import java.util.Collection;
import java.util.Collections;

public class UserMapper {

    public static User mapToUser(RegisterRequest request){
       User user = new User();
       user.setFullName(request.getFullName());
       user.setEmail(request.getEmail());
       user.setPassword(request.getPassword());  // 🔒 Will encode later in service
       user.setRoles(Collections.singleton(Role.USER)); // Default role
       return user;
    }
}
