package com.atguigu.gmall.list.service.impl;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.es.SkuBaseAttrEsVo;
import com.atguigu.gmall.es.SkuInfoEsVo;
import com.atguigu.gmall.es.SkuSearchParamESVo;
import com.atguigu.gmall.es.SkuSearchResultEsVo;
import com.atguigu.gmall.list.constant.EsConstant;
import com.atguigu.gmall.manager.BaseAttrInfo;
import com.atguigu.gmall.manager.sku.SkuEsService;
import com.atguigu.gmall.manager.sku.SkuInfo;
import com.atguigu.gmall.manager.sku.SkuManagerService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.Update;
import io.searchbox.core.search.aggregation.MetricAggregation;
import io.searchbox.core.search.aggregation.TermsAggregation;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class SkuEsServiceImpl implements SkuEsService {
    @Reference
    SkuManagerService skuManagerService;
    @Autowired
    JestClient jestClient;
    @Async//异步方法
    @Override
    public void onSale(Integer skuId) {
        SkuInfo info = skuManagerService.getSkuInfoBySkuId(skuId);
        log.info("获取到的商品详细信息是: {}", info);
        //将查询到的skuinfo对象拷贝到SkuInfoEsVo中,再存到es中
        SkuInfoEsVo skuInfoEsVo = new SkuInfoEsVo();
        BeanUtils.copyProperties(info, skuInfoEsVo);
        //还需要将平台属性的id查询出来再设置进skuInfoEsVo中
         List<SkuBaseAttrEsVo> vo = skuManagerService.getSkuBaseAttrValueIdBySkuId(skuId);
         skuInfoEsVo.setBaseAttrEsVos(vo);

         //保存到es中
        Index index = new Index.Builder(skuInfoEsVo).index(EsConstant.GMALL_INDEX)
                .type(EsConstant.GMALL_SKU_TYPE).id(skuInfoEsVo.getId().toString()).build();

        try {
            jestClient.execute(index);
        } catch (IOException e) {

            log.error("保存es中失败了,{}",e);
        }

    }
    /**
     * 按照查询参数查出页面需要的数据
     * @param paramEsVo
     * @return
     */
    @Override
    public SkuSearchResultEsVo searchSkuFromES(SkuSearchParamESVo paramEsVo) {
        SkuSearchResultEsVo resultEsVo = null;
        // jestClient.execute(xxx)
        //0、DSL的大拼串
        String queryDsl = buildSkuSearchQueryDSL(paramEsVo);

        //1、传入dsl语句
        Search search = new Search.Builder(queryDsl)
                .addIndex(EsConstant.GMALL_INDEX)
                .addType(EsConstant.GMALL_SKU_TYPE)
                .build();

        //2、执行查询
        try {
            SearchResult result = jestClient.execute(search);

            //===3、把查出出来的result处理成能给页面返回的SkuSearchResultEsVo对象
            resultEsVo = buildSkuSearchResult(result);
            resultEsVo.setPageNo(paramEsVo.getPageNo());
            return  resultEsVo;
        } catch (IOException e) {
            log.error("ES查询出故障：{}",e);
        }
        return resultEsVo;
    }


    @Async
    @Override
    public void updateHotScore(Integer skuId, Long hincrBy) {
        String updateHotScore = "{\"doc\": {\"hotScore\":"+hincrBy+"}}";
        Update update = new Update.Builder(updateHotScore)
                .index(EsConstant.GMALL_INDEX)
                .type(EsConstant.GMALL_SKU_TYPE)
                .id(skuId+"")
                .build();
        try {
            jestClient.execute(update);
        } catch (IOException e) {
            log.error("ES更新热度出问题了：{}",e);
        }
    }

    //构造QueryDSL字符串
    private    String buildSkuSearchQueryDSL(SkuSearchParamESVo paramEsVo){
        //1、创建一个搜索数据的构建器。帮我们能构造出DSL
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        //
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        //过滤三级分类信息
        if(paramEsVo.getCatalog3Id()!=null){
            //过滤三级分类信息
            TermQueryBuilder termCatalog3 = new TermQueryBuilder("catalog3Id",paramEsVo.getCatalog3Id());
            boolQuery.filter(termCatalog3);
        }

        //过滤valueId信息
        if(paramEsVo.getValueId()!=null && paramEsVo.getValueId().length>0){
            for (Integer vid : paramEsVo.getValueId()) {
                //过滤 页面提交来的所有valueId
                TermQueryBuilder termValueId = new TermQueryBuilder("baseAttrEsVos.valueId", vid);
                boolQuery.filter(termValueId);
            }
            ;
        }


        //搜索
        if(!StringUtils.isEmpty(paramEsVo.getKeyWord())){
            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName", paramEsVo.getKeyWord());
            boolQuery.must(matchQueryBuilder);

            //高亮
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuName");
            highlightBuilder.preTags("<span style='color:red'>");
            highlightBuilder.postTags("</span>");
            sourceBuilder.highlight(highlightBuilder);
        }



        //以上查询与过滤完成
        sourceBuilder.query(boolQuery);




        //排序
        if(!StringUtils.isEmpty(paramEsVo.getSortField())){
            SortOrder sortOrder = null;
            switch (paramEsVo.getSortOrder()){
                case "desc":sortOrder = SortOrder.DESC;break;
                case "asc":sortOrder = SortOrder.ASC;break;
                default: sortOrder = SortOrder.DESC;
            }
            sourceBuilder.sort(paramEsVo.getSortField(), sortOrder);
        }


        //分页
        //页面传入的是页码，我们计算一下从第几个开始查;  (pageNo - 1)*pageSize   0  12
        sourceBuilder.from( (paramEsVo.getPageNo()-1)*paramEsVo.getPageSize());
        sourceBuilder.size(paramEsVo.getPageSize());



        //聚合
        TermsBuilder termsBuilder = new TermsBuilder("valueIdAggs");
        termsBuilder.field("baseAttrEsVos.valueId");
        sourceBuilder.aggregation(termsBuilder);



        //他的string方法就是获取到我们的dsl语句
        String dsl = sourceBuilder.toString();
        return dsl;
    }

    //将查出的结果构建为页面能用的vo对象数据
    private SkuSearchResultEsVo buildSkuSearchResult( SearchResult result){
        SkuSearchResultEsVo resultEsVo = new SkuSearchResultEsVo();

        //所有skuInfo的集合
        List<SkuInfoEsVo> skuInfoEsVoList =  null;
        //1、从es搜索的结果中找到所有的SkuInfo信息

        //拿到命中的所有记录
        List<SearchResult.Hit<SkuInfoEsVo, Void>> hits = result.getHits(SkuInfoEsVo.class);
        if(hits == null || hits.size() == 0){
            return  null;
        }else{
            //查到了数据
            skuInfoEsVoList = new ArrayList<>(hits.size());
            //遍历所有命中的记录，取出每一个SKuInfo放在list中，并且设置好高亮
            for (SearchResult.Hit<SkuInfoEsVo, Void> hit : hits) {
                SkuInfoEsVo source = hit.source;

                //有可能有高亮的
                Map<String, List<String>> highlight = hit.highlight;
                //普通非全文模糊【匹配的是没有高亮的
                if(highlight!=null){
                    String higtText = highlight.get("skuName").get(0);
                    //替换高亮
                    source.setSkuName(higtText);
                }

                skuInfoEsVoList.add(source);
            }
        }



        //保存了skuInfo信息
        resultEsVo.setSkuInfoEsVos(skuInfoEsVoList);
        //总计录数
        resultEsVo.setTotal(result.getTotal().intValue());



        //从聚合的数据中取出所有平台属性以及他的值
        List<BaseAttrInfo> baseAttrInfos = getBaseAttrInfoGroupByValueId(result);
        resultEsVo.setBaseAttrInfos(baseAttrInfos);
        return  resultEsVo;
    }

    /**
     * 根据es中查询到的聚合的结果找到所有涉及到的平台属性对应的值
     * @param result
     * @return
     */
    private List<BaseAttrInfo> getBaseAttrInfoGroupByValueId(SearchResult result){

        MetricAggregation aggregations = result.getAggregations();
        //1、获取term聚合出来的数据
        TermsAggregation valueIdAggs = aggregations.getTermsAggregation("valueIdAggs");
        List<TermsAggregation.Entry> buckets = valueIdAggs.getBuckets();


        List<Integer> valueIds = new ArrayList<>();
        //2、遍历buckets
        for (TermsAggregation.Entry bucket : buckets) {
            String key = bucket.getKey();
            valueIds.add(Integer.parseInt(key));
        }

        //3、查询所有涉及到的平台属性以及值
        return  skuManagerService.getBaseAttrInfoGroupByValueId(valueIds);
    }


}
