package com.chenlong.utils;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by chenlong on 2017/8/21.
 */
@Component
public class FileEditUtils {

    Logger logger = Logger.getLogger(FileEditUtils.class);

    @Value("${file.path}")
    private  String filePath ;

    @Value("${file.name}")
    private String fileName;





    /**
     * 根據server status 修改文件
     * @param zone  zone名称
     * @param server  服务
     * @param status 状态
     * @throws IOException
     */
    @Deprecated
    public synchronized void enditFile(String zone, String server,String status) throws IOException {
        List<String> list = FileUtils.readLines(new File(filePath + fileName), "utf-8");

        if(list!= null){
            int i=0;
            for (; i<list.size();i++) {
                String str = list.get(i);
                if(!str.startsWith("#")  && str.contains(zone) && set.contains(status)){
                  break;
                }
            }
            boolean isAdd= true;
            int modCount =0;
            for ( ; i < list.size(); i++){
                String str = list.get(i);
                //down
                if(str.contains(server) && !str.contains(Constants.down) && status.equalsIgnoreCase(Constants.down)){
                    str = str.replace(";"," down;");
                    list.set(i,str);
                    modCount ++;
                    break;
                }
                //up
                if(str.contains(server) && str.contains(Constants.down) && status.equalsIgnoreCase(Constants.up)){
                    str = str.replace("down","");
                    list.set(i,str);
                    modCount ++;
                    break;
                }

                //remove
                if(str.contains(server) &&  status.equalsIgnoreCase(Constants.remove)){
                    str ="";
                    list.set(i,str);
                    modCount ++;
                    break;
                }

                if(str.contains(server)){
                    isAdd = false;
                }

                //upstream 对应zone的最后一行
                if(str.contains("\\}")){
                    // add
                    if(isAdd && status.equalsIgnoreCase(Constants.add) ){
                        str = "\r\n" + "\t\t\t\tserver " + server +" max_fails=2 fail_timeout=30s; ";
                        list.set(i-1,str);
                        modCount ++;
                    }

                    break;
                }
            }
            if(modCount >0){
                FileUtils.writeLines(new File(filePath + fileName),list);
            }
        }
    }

    /**
     * 修改文件
     * @param zone
     * @param servers
     * @throws IOException
     */
    public synchronized   void updataServer(String zone, String servers) throws IOException {
        List<String> list = FileUtils.readLines(new File(filePath + fileName), "utf-8");
        int i =0;
        //定位zone 的位置
        for (; i<list.size();i++) {
            String str = list.get(i);
            if(!str.startsWith("#")  && str.contains(zone)){
                i++;
                break;
            }
        }
        int count =0;
        //定位zone 最后一个"}"的位置
        for (; i < list.size();) {
            String line2 = list.get(i);
            if(line2.contains("}")){
                break;
            }else{
                list.remove(i);
            }
            //避免死循环
            count ++;
            if(count >100){
                break;
            }
        }

        list.set(i,servers.replace("server","\t\t\tserver") +"\t\t}");

        FileUtils.writeLines(new File(filePath + fileName),list);
    }

   static   Set set = new HashSet<>();

    static {
        set.add(Constants.add);
        set.add(Constants.down);
        set.add(Constants.remove);
        set.add(Constants.up);
    }

    public static Set getSet() {
        return set;
    }

    public static void setSet(Set set) {
        FileEditUtils.set = set;
    }
}
