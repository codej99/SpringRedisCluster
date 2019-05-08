package com.redis.cluster.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redis.cluster.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Collections;

import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RedisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private long msrl;

    @Before
    public void setUp() throws Exception {
        User user = User.builder()
                .uid("happydaddy@naver.com")
                .name("happydaddy")
                .password("password")
                .roles(Collections.singletonList("ROLE_USER"))
                .build();
        MvcResult action = mockMvc.perform(post("/redis/user")
                .header("Accept", "application/json;charset=UTF-8")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msrl").value(greaterThan(0)))
                .andExpect(jsonPath("$.uid").value("happydaddy@naver.com"))
                .andExpect(jsonPath("$.name").value("happydaddy"))
                .andExpect(jsonPath("$.roles").isArray())
                .andReturn();

        String resultString = action.getResponse().getContentAsString();
        JacksonJsonParser jsonParser = new JacksonJsonParser();
        msrl = Long.valueOf(jsonParser.parseMap(resultString).get("msrl").toString());
    }

    @Test
    public void A_getUser() throws Exception {
        mockMvc.perform(get("/redis/user/" + msrl))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msrl").value(msrl))
                .andExpect(jsonPath("$.uid").value("happydaddy@naver.com"))
                .andExpect(jsonPath("$.name").value("happydaddy"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"));
    }

    @Test
    public void B_putUser() throws Exception {
        User user = User.builder()
                .msrl(msrl)
                .uid("happydaddy@naver.com")
                .name("happydaddy_re")
                .roles(Collections.singletonList("ROLE_ADMIN")).build();
        mockMvc.perform(put("/redis/user")
                .header("Accept", "application/json;charset=UTF-8")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msrl").value(msrl))
                .andExpect(jsonPath("$.uid").value("happydaddy@naver.com"))
                .andExpect(jsonPath("$.name").value("happydaddy_re"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_ADMIN"));
    }

    @After
    public void delUser() throws Exception {
        mockMvc.perform(delete("/redis/user/" + msrl))
                .andDo(print())
                .andExpect(status().isOk());

    }
}