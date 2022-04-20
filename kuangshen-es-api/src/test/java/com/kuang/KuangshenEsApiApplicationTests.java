package com.kuang;

import com.alibaba.fastjson.JSON;
import com.kuang.pojo.User;
import org.apache.lucene.util.QueryBuilder;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;


/*
* 高级客户端测试 API
* */
@SpringBootTest
class KuangshenEsApiApplicationTests {

	@Autowired
	@Qualifier("restHighLevelClient")
	private RestHighLevelClient client;

	// 测试索引的创建 Request
	@Test
	void testCreateIndex() throws IOException {
		// 1.创建索引请求
		CreateIndexRequest request = new CreateIndexRequest("kuang_index");
		// 2.客户端执行请求，IndicesClient，请求后获得响应
		CreateIndexResponse response = client.indices().create(request, RequestOptions.DEFAULT);
		System.out.println(response);
	}

	// 测试获取索引
	@Test
	void testExistIndex() throws IOException {
		GetIndexRequest request = new GetIndexRequest("kuang_index");
		boolean exists = client.indices().exists(request, RequestOptions.DEFAULT);
		System.out.println(exists);
	}

	// 测试删除索引
	@Test
	void testDeleteIndex() throws IOException {
		DeleteIndexRequest request = new DeleteIndexRequest("kuang_index");
		AcknowledgedResponse delete = client.indices().delete(request, RequestOptions.DEFAULT);
		System.out.println(delete.isAcknowledged());
	}

	// 测试添加文档
	@Test
	void testAddDocument() throws IOException {
		// 创建对象
		User user = new User("狂神说", 3);
		// 创建请求
		IndexRequest request = new IndexRequest("kuang_index");

		// 规则 put /kuang_index/_doc/1
		request.id("1");
		request.timeout(TimeValue.timeValueSeconds(1));
		request.timeout("1s");

		// 将我们的数据放入请求 json
		request.source(JSON.toJSONString(user), XContentType.JSON);

		// 客户端发送请求，获取响应结果
		IndexResponse indexResponse = client.index(request, RequestOptions.DEFAULT);

		System.out.println(indexResponse);
		System.out.println(indexResponse.status()); // 对应我们命令的返回状态 created
	}

	// 获取文档，判断是否存在 get /index/doc/1
	@Test
	void testIsExists() throws IOException {
		GetRequest getRequest = new GetRequest("kuang_index", "1");
		//不获取返回的 _source的上下文了
		getRequest.fetchSourceContext(new FetchSourceContext(false));
		getRequest.storedFields("_none_");

		boolean exists = client.exists(getRequest, RequestOptions.DEFAULT);
		System.out.println(exists);
	}

	// 获取文档的信息
	@Test
	void testGetDocument() throws IOException {
		GetRequest getRequest = new GetRequest("kuang_index", "1");
		GetResponse getResponse = client.get(getRequest, RequestOptions.DEFAULT);
		System.out.println(getResponse.getSourceAsString()); // 打印文档内容
		System.out.println(getResponse); // 返回的全部内容和命令是一样的
	}

	// 更新文档的信息
	@Test
	void testUpdateRequest() throws IOException {
		UpdateRequest updateRequest = new UpdateRequest("kuang_index", "1");
		updateRequest.timeout("1s");

		User user = new User("狂神说Java", 18);
		updateRequest.doc(JSON.toJSONString(user), XContentType.JSON);

		UpdateResponse updateResponse = client.update(updateRequest, RequestOptions.DEFAULT);
		System.out.println(updateResponse.status());
	}

	// 删除文档记录
	@Test
	void testDeleteRequest() throws IOException {
		DeleteRequest deleteRequest = new DeleteRequest("kuang_index", "1");
		deleteRequest.timeout("1s");

		DeleteResponse deleteResponse = client.delete(deleteRequest, RequestOptions.DEFAULT);
		System.out.println(deleteResponse.status());
	}

	// 特殊的，实际项目中一般都会使用批量插入数据
	@Test
	void testBulkRequest() throws IOException {
		BulkRequest bulkRequest = new BulkRequest();
		bulkRequest.timeout("10s");

		ArrayList<User> userList = new ArrayList<>();
		userList.add(new User("kuangshen1", 3));
		userList.add(new User("kuangshen2", 3));
		userList.add(new User("kuangshen3", 3));
		userList.add(new User("wangli1", 3));
		userList.add(new User("wangli2", 3));

		// 批处理请求
		for (int i = 0; i < userList.size(); i++) {
			// 批量更新和删除，就在这里修改对应的请求即可
			bulkRequest.add(
					new IndexRequest("kuang_index")
					.id("" + (i + 1))
					.source(JSON.toJSONString(userList.get(i)), XContentType.JSON));
		}

		BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);
		System.out.println(bulkResponse.hasFailures()); // 是否失败方法，返回false代表成功
	}

	// 查询
	@Test
	void testSearch() throws IOException {
		SearchRequest searchRequest = new SearchRequest("kuang_index");

		//构建搜索条件
		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

		// 查询条件，我们可以使用QueryBuilders工具来实现
		// QueryBuilders.termQuery() 精准查询
		// QueryBuilders.matchAllQuery() 匹配所有
		TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("name", "wangli1");
		// MatchAllQueryBuilder matchAllQueryBuilder = QueryBuilders.matchAllQuery();
		sourceBuilder.query(termQueryBuilder);
		sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

		searchRequest.source(sourceBuilder);

		SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
		System.out.println(JSON.toJSONString(searchResponse.getHits()));
		System.out.println("=======================================");
	}

}
