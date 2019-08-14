package com.artfactory.project01.todayart.kakao;

import lombok.extern.java.Log;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;

@Service
@Log
public class KakaoPay {
    private static final String HOST = "https://kapi.kakao.com";

    private KakaoPayReadyVO kakaoPayReadyVO;
    private KakaoPayApprovalVO kakaoPayApprovalVO;

    public String kakaoPayReady(){
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "KakaoAK fe28e9c152baf07902fb72a7e6120c29");
        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.add("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE+";charset=UTF-8");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
        params.add("cid", "TC0ONETIME");
        params.add("partner_order_id", "1001");
        params.add("partner_user_id", "gorany");
        params.add("item_name", "갤럭시노트10+");
        params.add("quantity", "1");
        params.add("total_amount", "2100");
        params.add("tax_free_amount", "100");
        params.add("approval_url", "http://localhost:8080/kakaoPaySuccess");
        params.add("cancel_url", "http://localhost:8080/kakaoPayCancel");
        params.add("fail_url", "http://localhost:8080/kakaoPaySuccessFail");

        HttpEntity<MultiValueMap<String, String>> body = new HttpEntity<>(params, headers);

        try{
            kakaoPayReadyVO = restTemplate.postForObject(new URI(HOST + "/v1/payment/ready"), body, KakaoPayReadyVO.class );

            log.info(""+kakaoPayReadyVO);

            return kakaoPayReadyVO.getNext_redirect_pc_url();
        }catch (RestClientException e){
            e.printStackTrace();
        }catch (URISyntaxException e){
            e.printStackTrace();
        }

        return "/kakaoPaySuccessFail";
    }

    public KakaoPayApprovalVO kakaoPayInfo(String pg_token){

        log.info("KakaoPayInfoVO............");
        log.info("------------------------");

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "KakaoAK fe28e9c152baf07902fb72a7e6120c29");
        headers.add("Accept", MediaType.APPLICATION_JSON_UTF8_VALUE);
        headers.add("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE+";charset=UTF-8");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("cid", "TC0ONETIME");
        params.add("tid", kakaoPayReadyVO.getTid());
        params.add("partner_order_id", "1001");
        params.add("partner_user_id", "gorany");
        params.add("pg_token", pg_token);
        params.add("total_amount", "2100");

        HttpEntity<MultiValueMap<String, String>> body = new HttpEntity<>(params, headers);

        try{
            kakaoPayApprovalVO = restTemplate.postForObject(new URI(HOST+"/v1/payment/approve"), body, KakaoPayApprovalVO.class);
            log.info(""+kakaoPayReadyVO);

            return kakaoPayApprovalVO;
        }catch(RestClientException e){
            e.printStackTrace();
        }catch (URISyntaxException e){
            e.printStackTrace();;
        }
        return null;
    }
}