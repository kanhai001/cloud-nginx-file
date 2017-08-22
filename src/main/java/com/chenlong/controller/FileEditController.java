package com.chenlong.controller;

import com.chenlong.utils.Constants;
import com.chenlong.utils.FileEditUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chenlong on 2017/8/21.
 *
 * 文件编辑操作了类
 */
@RestController
public class FileEditController {

    Logger logger = Logger.getLogger(FileEditController.class);

    @Value("${nginx.server.address}")
    private String serverAddress[];

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    FileEditUtils editUtils;

//    /**
//     * 查询 locatoin 对应upstream的服务列表
//     *
//     * @param location
//     * @param upstream
//     * @param verbose
//     * @return
//     */
//    @RequestMapping("/{location}")
//    @ResponseBody
//    public List exchangeServer(@PathVariable("location") String location,
//                            @RequestParam String upstream,
//                            @RequestParam(required = false) String server,
//                            @RequestParam(required = false) String status,
//
//                           String verbose ) throws IOException {
//        StringBuilder buf = new StringBuilder();
//        buf.append(location).append("?")
//                .append("upstream").append("=").append(upstream);
//
//
//        /**
//         * 操作server 的 up down add remove
//         */
//        if(!StringUtils.isEmpty(server) && FileEditUtils.getSet().contains(status)){
//            buf.append("&").append("server").append("=").append(server)
//                    .append("&").append(status).append("=");
//            //操作文件
//            editUtils.enditFile(upstream,server,status);
//        }
//
//        logger.info("serverAdd:"+ buf.toString());
//        List list = new ArrayList<>();
//        //针对多服务的情况
//        for (String serverAddr : serverAddress){
//            String result  = restTemplate.getForObject(serverAddr + buf.toString(),String.class);
//            list.add(result);
//        }
//
//        logger.info("result:"+ list);
//        return  list;
//    }


    /**
     * 查询 locatoin 对应upstream的服务列表
     *
     * @param location
     * @param upstream
     * @param verbose
     * @return
     */
    @RequestMapping("/{location}")
    @ResponseBody
    public List exchangeServer(@PathVariable("location") String location,
                               @RequestParam String upstream,
                               @RequestParam(required = false) String server,
                               @RequestParam(required = false) String status,
                               @RequestParam(required = false) String weight,
                               @RequestParam(required = false) String max_fails,
                               @RequestParam(required = false) String fail_timeout,
                               String verbose ) throws IOException {
        StringBuilder buf = new StringBuilder();
        buf.append(location).append("?")
                .append("upstream").append("=").append(upstream);

        /**
         * 操作server 的 up down add remove
         */
        if(!StringUtils.isEmpty(server)){
            buf.append("&").append("server").append("=").append(server);
            if(FileEditUtils.getSet().contains(status)) {
                  buf.append("&").append(status).append("=");
            }
            if(!StringUtils.isEmpty(weight)){
                buf.append("&").append("weight=").append(weight);
            }
            if(!StringUtils.isEmpty(max_fails)){
                buf.append("&").append("max_fails=").append(max_fails);
            }
            if(!StringUtils.isEmpty(fail_timeout)){
                buf.append("&").append("fail_timeout=").append(fail_timeout);
            }

        }
        logger.info("buf:"+ buf.toString());
        List list = new ArrayList<String>();
        //针对多服务的情况
        for (String serverAddr : serverAddress){
            String result  = restTemplate.getForObject(serverAddr + buf.toString(),String.class);
            list.add(result);
        }

        //发生操作
//        if(FileEditUtils.getSet().contains(status)){
            //查询最新的修改，并持久化到文件
            StringBuilder queryParem = new StringBuilder();
            queryParem.append(location).append("?")
                    .append("upstream").append("=").append(upstream)
                    .append("&").append("verbose=");
            String servers = restTemplate.getForObject(serverAddress[0] +queryParem.toString(),String.class);
            //操作文件
            editUtils.updataServer(upstream,servers);
//        }


        logger.info("result:"+ list);
        return  list;
    }



}
