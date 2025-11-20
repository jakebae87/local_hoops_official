package com.example.demo.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

@Mapper
public interface UserMapper {

    Map<String, Object> findByEmail(@Param("email") String email);

    boolean existsByEmail(@Param("email") String email);

    void insertUser(Map<String, Object> userData);
}
