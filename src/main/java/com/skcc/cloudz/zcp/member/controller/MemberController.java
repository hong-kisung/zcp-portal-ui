package com.skcc.cloudz.zcp.member.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.skcc.cloudz.zcp.common.vo.RtnVO;
import com.skcc.cloudz.zcp.member.service.MemberService;
import com.skcc.cloudz.zcp.member.vo.MemberVO;

import io.kubernetes.client.ApiException;
import io.kubernetes.client.models.V1ClusterRoleBinding;

@Configuration
@RestController
@RequestMapping("/iam/member")
public class MemberController {

	private static final Logger LOG = LoggerFactory.getLogger(MemberController.class);    
	
	@Autowired
	MemberService memberSvc;
	
	@RequestMapping("/userList")
	Object userList(HttpServletRequest httpServletRequest){
		RtnVO vo = new RtnVO();
		vo.setData(memberSvc.getUserList());
		return vo;
	}
	
	@RequestMapping("/modifyUser")
	Object modifyUser(HttpServletRequest httpServletRequest, @RequestBody MemberVO memberVO){
		RtnVO vo = new RtnVO();
		memberSvc.modifyUserAttribute(memberVO);
		return vo;
	}
	
	@RequestMapping("/createUser")
	Object createUser(HttpServletRequest httpServletRequest, @RequestBody MemberVO memberVO){
		RtnVO vo = new RtnVO();
		memberSvc.createUser(memberVO);
		return vo;
	}
	
	@RequestMapping("/CreateUser")
	Object deleteUser(HttpServletRequest httpServletRequest, @RequestBody MemberVO memberVO){
		RtnVO vo = new RtnVO();
		memberSvc.deleteUser(memberVO);
		return vo;
	}
	
//	@RequestMapping("/serviceAccount")
//	Object serviceAccount(HttpServletRequest httpServletRequest) throws IOException, ApiException{
//		RtnVO vo = new RtnVO();
////		ObjectMapper mapper = new ObjectMapper();
////		//Map<Object, Object> map = new HashMap<Object, Object>(); // convert JSON string to
////		String json = memberSvc.serviceAccount();
////		//Map map = mapper.readValue(json, new TypeReference<Map<Object, Object>>(){});
////		//vo.setData(map);
//		return vo;
//	}
	
	@RequestMapping("/clusterRoleList")
	Object clusterRoleList(HttpServletRequest httpServletRequest) throws IOException, ApiException, ParseException{
		RtnVO vo = new RtnVO();
		vo.setData(memberSvc.clusterRoleList());
		return vo;
	}
	
	@RequestMapping("/clusterRoleBindingList")
	Object clusterRoleBindingList(HttpServletRequest httpServletRequest) throws IOException, ApiException{
		RtnVO vo = new RtnVO();
		vo.setData(memberSvc.clusterRoleBindingList());
		return vo;
	}
	
	@RequestMapping("/serviceAccount")
	Object serviceAccount(HttpServletRequest httpServletRequest) throws IOException, ApiException{
		RtnVO vo = new RtnVO();
		vo.setData(memberSvc.serviceAccountList());
		return vo;
	}
	
	@RequestMapping("/createClusterRoleBinding")
	Object createClusterRoleBinding(HttpServletRequest httpServletRequest, @RequestBody V1ClusterRoleBinding data) throws IOException, ApiException{
		RtnVO vo = new RtnVO();
		vo.setData(memberSvc.createClusterRoleBinding(data));
		return vo;
	}
	
}