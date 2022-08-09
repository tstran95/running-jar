package com.vn.runjar.services;

import com.vn.runjar.model.ClassInfo;
import com.vn.runjar.response.Response;

public interface AppService {
    Response run(ClassInfo classInfo);
}
