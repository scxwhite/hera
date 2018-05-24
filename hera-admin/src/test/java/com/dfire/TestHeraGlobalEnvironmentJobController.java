package com.dfire;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.http.HttpSession;


/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 2:28 2018/1/11
 * @desc 集成mock单元测试，对controller restful请求 进行单元测试
 */
@ComponentScan(basePackages = "com.dfire")
@MapperScan(basePackages = "com.dfire.common.mapper")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = TestHeraGlobalEnvironmentJobController.class)
@WebAppConfiguration
public class TestHeraGlobalEnvironmentJobController {

    private MockMvc mvc;

    @Autowired
    WebApplicationContext webApplicationConnect;

    @Before
    public void setUp() throws Exception {
        mvc = MockMvcBuilders.webAppContextSetup(webApplicationConnect).build();
    }

    public void testGet() throws Exception{
        String expectedResult = "hello world!";
        String uri = "/jobs/";
        MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.get(uri).accept(MediaType.APPLICATION_JSON))
                .andReturn();
        int status = mvcResult.getResponse().getStatus();
        String content = mvcResult.getResponse().getContentAsString();
        Assert.assertTrue("错误，正确的返回值为200", status == 200);
        Assert.assertFalse("错误，正确的返回值为200", status != 200);
        Assert.assertTrue("数据一致", expectedResult.equals(content));
        Assert.assertFalse("数据不一致", !expectedResult.equals(content));
    }

    @Test
    public void testSuite() throws Exception{
        this.testGet();//获取用户
//        this.testPost();//创建一本书籍
//        this.testPut();//更新一本书籍
//        this.testDelete();//删除一本书籍
    }



}
