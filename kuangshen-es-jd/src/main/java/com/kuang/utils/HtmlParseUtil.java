package com.kuang.utils;

import com.kuang.pojo.Content;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Component
public class HtmlParseUtil {
//    public static void main(String[] args) throws IOException {
//        //测试
//        new HtmlParseUtil().parseID("java").forEach(System.out::println);
//    }

    //自定义方法：爬取京东页面的数据
    public List<Content> parseID(String keywords) throws IOException {
        //获得请求 https://search.jd.com/Search?keyword=java
        String url = "https://search.jd.com/Search?keyword=" + keywords;
        //解析网页（Jsoup返回的document就是Document对象）
        Document document = Jsoup.parse(new URL(url), 30000);
        //所有你在js中可以使用的方法，这里都能用
        Element element = document.getElementById("J_goodsList");
        //获取所有的li元素
        Elements elements = element.getElementsByTag("li");

        ArrayList<Content> goodslist = new ArrayList<>();

        //获取元素中的内容，这里el就是每一个li标签了
        for(Element el : elements){
            //关于这种图片特别多的网站，所有的图片都是延迟加载模式 source-data-lazy-img
            String img = el.getElementsByTag("img").eq(0).attr("data-lazy-img");
            String price = el.getElementsByClass("p-price").eq(0).text();
            String title = el.getElementsByClass("p-name").eq(0).text();

            Content content = new Content();
            content.setImg(img);
            content.setPrice(price);
            content.setTitle(title);

            //把返回结果封装成一个对象
            goodslist.add(content);
        }

        return goodslist;
    }
}
