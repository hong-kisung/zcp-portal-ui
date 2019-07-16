package com.skcc.cloudz.zcp.portal.management.namespace.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skcc.cloudz.zcp.api.iam.domain.vo.ApiResponseVo;
import com.skcc.cloudz.zcp.api.iam.service.impl.IamRestClient;
import com.skcc.cloudz.zcp.common.constants.ApiResult;
import com.skcc.cloudz.zcp.common.security.service.SecurityService;
import com.skcc.cloudz.zcp.common.util.NumberUtil;
import com.skcc.cloudz.zcp.portal.management.namespace.vo.EnquryNamespaceVO;
import com.skcc.cloudz.zcp.portal.management.namespace.vo.SecretDockerVO;
import com.skcc.cloudz.zcp.portal.management.namespace.vo.SecretDtlVO;
import com.skcc.cloudz.zcp.portal.management.namespace.vo.SecretTlsVO;
import com.skcc.cloudz.zcp.portal.management.namespace.vo.SecretVO;
import com.skcc.cloudz.zcp.portal.management.user.vo.ZcpNamespace;
import com.skcc.cloudz.zcp.portal.management.user.vo.ZcpNamespaceList;

@Service
public class NamespaceService {

	private final Logger logger = (Logger) LoggerFactory.getLogger(NamespaceService.class);

	@Value("${props.iam.baseUrl}")
	private String iamBaseUrl;

	@Autowired
	private IamRestClient client;

	@Autowired
	private SecurityService securityService;

	public Object getResourceQuota(EnquryNamespaceVO vo) throws Exception {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		String param = "userId=" + securityService.getUserDetails().getUserId();
		ApiResponseVo response = client.request("/iam/metrics/namespaces?" + param, null);

		if (response.getCode().equals("ZCP-0001")) {
			logger.debug(response.getMsg());
			List<Object> empty = new ArrayList<>();
			resultMap.put("items", empty);
			resultMap.put("errorMsg", response.getMsg());
			return resultMap;
		}

		if (!response.getCode().equals(ApiResult.SUCCESS.getCode())) {
			throw new Exception(response.getMsg());
		}

		ObjectMapper mapper = new ObjectMapper();
		ZcpNamespaceList namespaceList = mapper.convertValue(response.getData(), ZcpNamespaceList.class);
		List<ZcpNamespace> listQuotas = namespaceList.getItems();
		Stream<ZcpNamespace> stream = namespaceList.getItems().stream();

		if (!StringUtils.isEmpty(vo.getSortItem())) {
			switch (vo.getSortItem()) {
			case "namespace":
				if (vo.isSortOrder())
					stream = stream.sorted((a, b) -> a.getName().compareTo(b.getName()));// asc
				else
					stream = stream.sorted((a, b) -> b.getName().compareTo(a.getName()));
				break;
			case "cpuR":
				if (vo.isSortOrder())
					stream = stream.sorted((a, b) -> a.getUsedCpuRequests().compareTo(b.getUsedCpuRequests()));
				else
					stream = stream.sorted((a, b) -> b.getUsedCpuRequests().compareTo(a.getUsedCpuRequests()));
				break;
			case "cpuL":
				if (vo.isSortOrder())
					stream = stream.sorted((a, b) -> a.getUsedCpuLimits().compareTo(b.getUsedCpuLimits()));
				else
					stream = stream.sorted((a, b) -> b.getUsedCpuLimits().compareTo(a.getUsedCpuLimits()));
				break;
			case "memoryR":
				if (vo.isSortOrder())
					stream = stream.sorted((a, b) -> a.getUsedMemoryRequests().compareTo(b.getUsedMemoryRequests()));
				else
					stream = stream.sorted((a, b) -> a.getUsedMemoryRequests().compareTo(b.getUsedMemoryRequests()));
				break;
			case "memoryL":
				if (vo.isSortOrder())
					stream = stream.sorted((a, b) -> a.getUsedMemoryLimits().compareTo(b.getUsedMemoryLimits()));
				else
					stream = stream.sorted((a, b) -> a.getUsedMemoryLimits().compareTo(b.getUsedMemoryLimits()));
				break;
			case "user":
				if (vo.isSortOrder())
					stream = stream.sorted((a, b) -> NumberUtil.compare(a.getUserCount(), b.getUserCount()));
				else
					stream = stream.sorted((a, b) -> NumberUtil.compare(b.getUserCount(), a.getUserCount()));
				break;
			case "status":
				if (vo.isSortOrder())
					stream = stream.sorted((a, b) -> a.getStatus().compareTo(b.getStatus()));
				else
					stream = stream.sorted((a, b) -> b.getStatus().compareTo(a.getStatus()));
				break;
			case "createTime":
				if (vo.isSortOrder())
					stream = stream.sorted((a, b) -> a.getCreationDate().compareTo(b.getCreationDate()));
				else
					stream = stream.sorted((a, b) -> b.getCreationDate().compareTo(a.getCreationDate()));
				break;
			}
		}

		if (!StringUtils.isEmpty(vo.getNamespace())) {
			stream = stream.filter((namespace) -> {
				if (namespace != null)
					return namespace.getName().indexOf(vo.getNamespace()) > -1;
				else
					return false;
			});
		}

		if (stream != null)
			listQuotas = stream.collect(Collectors.toList());

		namespaceList.setItems(listQuotas);
		return namespaceList;
	}

	public Map<String, Object> getResourceLabel() throws Exception {
		Map<String, Object> resultMap = new HashMap<String, Object>();

		ApiResponseVo response = client.request(HttpMethod.GET, "/iam/namespace/labels", null);
		if (!response.getCode().equals(ApiResult.SUCCESS.getCode())) {
			throw new Exception(response.getMsg());
		}

		resultMap.putAll(response.getData());

		return resultMap;
	}

	public void deleteNamespace(String namespace) throws Exception {
		// Map<String, String> param = new HashMap<>();
		// param.put("userId", securityService.getUserDetails().getUserId());
		String param = "?userId=" + securityService.getUserDetails().getUserId();
		String url = "/iam/namespace/" + namespace + param;
		ApiResponseVo response = client.request(HttpMethod.DELETE, url, null);
		if (!response.getCode().equals(ApiResult.SUCCESS.getCode())) {
			throw new Exception(response.getMsg());
		}
	}

	public Map<String, Object> getNamespaceResource(String namespace) throws Exception {
		if (StringUtils.isEmpty(namespace))
			return null;
		Map<String, Object> resultMap = new HashMap<String, Object>();
		String param = "userId=" + securityService.getUserDetails().getUserId();
		String url = "/iam/namespace/{namespace}/resource?" + param;
		ApiResponseVo response = client.request(HttpMethod.GET, url.replace("{namespace}", namespace), null);
		if (!response.getCode().equals(ApiResult.SUCCESS.getCode())) {
			throw new Exception(response.getMsg());
		}

		resultMap.putAll(response.getData());

		return resultMap;
	}

	public void createNamespace(HashMap<String, Object> data) throws Exception {
		ApiResponseVo response = client.request(HttpMethod.POST, "/iam/namespace", data);
		if (!response.getCode().equals(ApiResult.SUCCESS.getCode())) {
			logger.debug(response.getMsg());
			throw new Exception(response.getMsg());
		}
	}

	public Map<String, Object> getUsers(HashMap<String, String> data) throws Exception {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		String namespace = data.get("namespace");
		ApiResponseVo response = null;
		if (StringUtils.isEmpty(namespace)) {
			response = client.request(HttpMethod.GET, "/iam/users", null);
			if (!response.getCode().equals(ApiResult.SUCCESS.getCode())) {
				throw new Exception(response.getMsg());
			}

		} else {
			response = client.request(HttpMethod.GET, "/iam/namespace/" + namespace + "/users", null);
			if (!response.getCode().equals(ApiResult.SUCCESS.getCode())) {
				throw new Exception(response.getMsg());
			}
		}
		String searchWord = data.get("searchWord");
		if (StringUtils.isNoneEmpty(searchWord)) {
			List<Object> newUser = new ArrayList();
			List<Map<String, String>> users = (List<Map<String, String>>) ((Map<String, Object>) response.getData())
					.get("items");
			for (Map<String, String> user : users) {
				if (user.get("username").indexOf(searchWord) > -1) {
					newUser.add(user);
				}
				if (user.get("email") != null && user.get("email").indexOf(searchWord) > -1) {
					newUser.add(user);
				}
				if (user.get("lastName") != null && user.get("lastName").indexOf(searchWord) > -1) {
					newUser.add(user);
				}
				if (user.get("firstName") != null && user.get("firstName").indexOf(searchWord) > -1) {
					newUser.add(user);
				}
				if (user.get("firstName") != null && user.get("lastName") != null
						&& (user.get("firstName") + user.get("lastName")).indexOf(searchWord) > -1) {
					newUser.add(user);
				}
				newUser = newUser.stream().distinct().collect(Collectors.toList());
			}
			Map<String, Object> rtnData = new HashMap<>();
			rtnData.put("items", newUser);

			return rtnData;
		}

		resultMap.putAll(response.getData());

		return resultMap;
	}

	public void addUserInNamespace(HashMap<String, Object> data) throws Exception {
		ApiResponseVo resUser = client.request(HttpMethod.POST,
				"/iam/namespace/" + data.get("namespace") + "/roleBinding", data);
		if (!resUser.getCode().equals(ApiResult.SUCCESS.getCode())) {
			throw new Exception(resUser.getMsg());
		}

	}

	public void modifyNamespaceRole(HashMap<String, Object> data) throws Exception {
		ApiResponseVo resUser = client.request(HttpMethod.PUT,
				"/iam/namespace/" + data.get("namespace") + "/roleBinding", data);
		if (!resUser.getCode().equals(ApiResult.SUCCESS.getCode())) {
			throw new Exception(resUser.getMsg());
		}

	}

	public void delNamespaceRole(HashMap<String, Object> data) throws Exception {
		ApiResponseVo resUser = client.request(HttpMethod.DELETE,
				"/iam/namespace/" + data.get("namespace") + "/roleBinding", data);
		if (!resUser.getCode().equals(ApiResult.SUCCESS.getCode())) {
			throw new Exception(resUser.getMsg());
		}

	}

	public void addLableOfNamespace(HashMap<String, Object> data) throws Exception {
		ApiResponseVo resUser = client.request(HttpMethod.POST, "/iam/namespace/" + data.get("namespace") + "/label",
				data);
		if (!resUser.getCode().equals(ApiResult.SUCCESS.getCode())) {
			throw new Exception(resUser.getMsg());
		}

	}

	public Map<String, Object> getLableOfNamespace(HashMap<String, Object> data) throws Exception {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		ApiResponseVo response = client.request(HttpMethod.GET, "/iam/namespace/" + data.get("namespace") + "/labels",
				null);
		if (!response.getCode().equals(ApiResult.SUCCESS.getCode())) {
			throw new Exception(response.getMsg());
		}
		resultMap.putAll(response.getData());
		return resultMap;

	}

	public void delLableOfNamespace(HashMap<String, Object> data) throws Exception {
		ApiResponseVo resUser = client.request(HttpMethod.DELETE, "/iam/namespace/" + data.get("namespace") + "/label",
				data);
		if (!resUser.getCode().equals(ApiResult.SUCCESS.getCode())) {
			throw new Exception(resUser.getMsg());
		}
	}

	public List<String> getNamespaceRoles() throws Exception {
		List<String> clusterRoles = new ArrayList<String>();
		ApiResponseVo apiResponseVo = client.request(HttpMethod.GET, "/iam/rbac/clusterRoles?type=namespace", null);
		if (!apiResponseVo.getCode().equals(ApiResult.SUCCESS.getCode())) {
			throw new Exception(apiResponseVo.getMsg());
		}
		Map<String, Object> data = apiResponseVo.getData();
		List<HashMap<String, Object>> items = (List<HashMap<String, Object>>) data.get("items");

		for (HashMap<String, Object> item : items) {
			clusterRoles.add(((HashMap<String, Object>) item.get("metadata")).get("name").toString());
		}

		return clusterRoles;

	}

	@SuppressWarnings("unchecked")
	public List<SecretVO> getSecrets(String namespace) throws Exception {
		ApiResponseVo response = client.request(HttpMethod.GET, "/iam/namespace/" + namespace + "/secrets", null);

		if (!response.getCode().equals(ApiResult.SUCCESS.getCode())) {
			throw new Exception(response.getMsg());
		}

		Map<String, Object> data = response.getData();
		List<HashMap<String, Object>> items = (List<HashMap<String, Object>>) data.get("items");

		List<SecretVO> resultList = new ArrayList<SecretVO>();

		for (HashMap<String, Object> item : items) {
			final HashMap<String, Object> meta = (HashMap<String, Object>) item.get("metadata");
			final HashMap<String, Object> annotations = (HashMap<String, Object>) meta.get("annotations");
			final long creationTimestamp = (Long) meta.get("creationTimestamp");

			SecretVO secretVO = new SecretVO();
			secretVO.setName(meta.get("name").toString());

			Object type = item.get("type");
			if ("kubernetes.io/dockerconfigjson".equals(type)) {
				secretVO.setType("Docker Registry");
			} else if ("kubernetes.io/tls".equals(type)) {
				secretVO.setType("TLS");
			} else {
				secretVO.setType("");
			}

			if (annotations != null && annotations.get("annotations") != null) {
				secretVO.setLabel(annotations.get("cloudzcp.io/description").toString());
			} else {
				secretVO.setLabel("");
			}

			String date = DateFormatUtils.format(creationTimestamp, "yyyy-MM-dd HH:mm:ss");
			// String date = ((HashMap<String, Object>) ((HashMap<String, Object>) item.get("metadata"))
			// 		.get("creationTimestamp")).get("year")
			// 		+ "/"
			// 		+ String.format("%02d",
			// 				((HashMap<String, Object>) ((HashMap<String, Object>) item.get("metadata"))
			// 						.get("creationTimestamp")).get("monthOfYear"))
			// 		+ "/"
			// 		+ String.format("%02d",
			// 				((HashMap<String, Object>) ((HashMap<String, Object>) item.get("metadata"))
			// 						.get("creationTimestamp")).get("dayOfMonth"))
			// 		+ " "
			// 		+ String.format("%02d",
			// 				((HashMap<String, Object>) ((HashMap<String, Object>) item.get("metadata"))
			// 						.get("creationTimestamp")).get("hourOfDay"))
			// 		+ ":"
			// 		+ String.format("%02d",
			// 				((HashMap<String, Object>) ((HashMap<String, Object>) item.get("metadata"))
			// 						.get("creationTimestamp")).get("minuteOfHour"))
			// 		+ ":"
			// 		+ String.format("%02d", ((HashMap<String, Object>) ((HashMap<String, Object>) item.get("metadata"))
			// 				.get("creationTimestamp")).get("secondOfMinute"));

			secretVO.setDate(date);
			resultList.add(secretVO);
		}

		return resultList;
	}

	public Map<String, Object> createDockerSecret(Map<String, Object> params) throws Exception {
		String url = UriComponentsBuilder.fromUriString(iamBaseUrl).path("/iam/namespace/{namespace}/secret/new/docker")
				.buildAndExpand(params.get("pNamespace")).toString();

		SecretDockerVO secretDockerParam = new SecretDockerVO();

		secretDockerParam.setEmail(params.get("pDocker_email").toString());
		secretDockerParam.setName(params.get("pSecret_name").toString());
		secretDockerParam.setPassword(params.get("pDocker_password").toString());
		secretDockerParam.setServer(params.get("pDocker_server").toString());
		secretDockerParam.setType("kubernetes.io/dockerconfigjson");
		secretDockerParam.setUsername(params.get("pDocker_username").toString());
		secretDockerParam.setDescription(params.get("pLabel").toString());

		ApiResponseVo response = new ApiResponseVo();
		Map<String, Object> resultData = null;

		try {
			HttpEntity<SecretDockerVO> requestEntity = null;

			HttpHeaders headers = new HttpHeaders();

			headers.setAccept(Arrays.asList(new MediaType[] { MediaType.APPLICATION_JSON }));
			headers.setContentType(MediaType.APPLICATION_JSON);

			requestEntity = new HttpEntity<SecretDockerVO>(secretDockerParam, headers);
			RestTemplate restTemplate = new RestTemplate();
			ResponseEntity<ApiResponseVo> entity = restTemplate.exchange(url, HttpMethod.POST, requestEntity,
					ApiResponseVo.class);

			if (entity != null && entity.getStatusCode() == HttpStatus.OK) {
				response.setCode(entity.getBody().getCode());
				response.setMsg(entity.getBody().getMsg());

				response.setData(entity.getBody().getData());
				resultData = response.getData();
			}
		} catch (RestClientException e) {
			e.printStackTrace();
		}

		if (!response.getCode().equals(ApiResult.SUCCESS.getCode())) {
			throw new Exception(response.getMsg());
		}

		return resultData;
	}

	public Map<String, Object> createTlsSecret(HttpServletRequest request) throws Exception {
		String param = "?name=" + request.getParameter("pSecret_name") + "&type=kubernetes.io/tls" + "&description="
				+ request.getParameter("pLabel");

		String url = UriComponentsBuilder.fromUriString(iamBaseUrl)
				.path("/iam/namespace/{namespace}/secret/new/tls" + param)
				.buildAndExpand(request.getParameter("pNamespace")).toString();

		SecretTlsVO secretTlsParam = new SecretTlsVO();

		MultipartHttpServletRequest multipartHttpServletRequest = (MultipartHttpServletRequest) request;
		Iterator<String> iterator = multipartHttpServletRequest.getFileNames();
		MultipartFile multipartFile = null;

		int count = 0;
		while (iterator.hasNext()) {
			multipartFile = multipartHttpServletRequest.getFile(iterator.next());

			if (multipartFile.isEmpty() == false) {
				if (count == 0) {
					secretTlsParam.setCrt(multipartFile.getBytes());
					secretTlsParam.setCrtFileName(multipartFile.getOriginalFilename());

				} else {
					secretTlsParam.setKey(multipartFile.getBytes());
					secretTlsParam.setKeyFileName(multipartFile.getOriginalFilename());
				}
			}
			count++;
		}

		ByteArrayResource crt = new ByteArrayResource(secretTlsParam.getCrt()) {
			@Override
			public String getFilename() {
				return secretTlsParam.getCrtFileName();
			}
		};

		ByteArrayResource key = new ByteArrayResource(secretTlsParam.getKey()) {
			@Override
			public String getFilename() {
				return secretTlsParam.getKeyFileName();
			}
		};

		LinkedMultiValueMap<String, Object> bodyMap = new LinkedMultiValueMap<>();

		bodyMap.add("crt", crt);
		bodyMap.add("key", key);

		ApiResponseVo response = new ApiResponseVo();
		Map<String, Object> resultData = null;
		
		try {
			HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = null;
			
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.MULTIPART_FORM_DATA);
			
			requestEntity = new HttpEntity<>(bodyMap, headers);
			RestTemplate restTemplate = new RestTemplate();
			ResponseEntity<ApiResponseVo> entity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, ApiResponseVo.class);
			
			if (entity != null && entity.getStatusCode() == HttpStatus.OK) {
				response.setCode(entity.getBody().getCode());
				response.setMsg(entity.getBody().getMsg());

				response.setData(entity.getBody().getData());
				resultData = response.getData();
			}
			
		} catch (RestClientException e) {
			e.printStackTrace();
		}

		if (!response.getCode().equals(ApiResult.SUCCESS.getCode())) {
			throw new Exception(response.getMsg());
		}
		
		return resultData;
	}

	@SuppressWarnings("unchecked")
	public SecretDtlVO getSecretDtl(String namespace, String secret) throws Exception {
		String param = "?namespace=" + namespace + "&secret=" + secret;

		String url = UriComponentsBuilder.fromUriString(iamBaseUrl)
				.path("/iam/namespace/{namespace}/secret/{secret}" + param).buildAndExpand(namespace, secret)
				.toString();

		ApiResponseVo response = new ApiResponseVo();

		try {
			HttpEntity<String> requestEntity = null;

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			requestEntity = new HttpEntity<String>(headers);

			RestTemplate restTemplate = new RestTemplate();
			ResponseEntity<ApiResponseVo> entity = restTemplate.exchange(url, HttpMethod.GET, requestEntity,
					ApiResponseVo.class);

			if (entity != null && entity.getStatusCode() == HttpStatus.OK) {
				response.setCode(entity.getBody().getCode());
				response.setMsg(entity.getBody().getMsg());
				response.setData(entity.getBody().getData());
			}
		} catch (RestClientException e) {
			e.printStackTrace();
		}

		if (!response.getCode().equals(ApiResult.SUCCESS.getCode())) {
			throw new Exception(response.getMsg());
		}

		Map<String, Object> data = response.getData();

		SecretDtlVO secretDtlVO = new SecretDtlVO();
		secretDtlVO.setType(data.get("type").toString());

		if ("kubernetes.io/dockerconfigjson".equals(data.get("type"))) {
			secretDtlVO.setServer(((HashMap<String, Object>) data.get("stringData")).get("server").toString());
			if ((HashMap<String, Object>) ((HashMap<String, Object>) data.get("metadata")).get("annotations") != null) {
				secretDtlVO.setLabel(
						((HashMap<String, Object>) ((HashMap<String, Object>) data.get("metadata")).get("annotations"))
								.get("cloudzcp.io/description").toString());
			} else {
				secretDtlVO.setLabel("");
			}
			secretDtlVO.setUsername(((HashMap<String, Object>) data.get("stringData")).get("username").toString());
			secretDtlVO.setPassword(((HashMap<String, Object>) data.get("stringData")).get("password").toString());

			if (((HashMap<String, Object>) data.get("stringData")).get("email") != null) {
				secretDtlVO.setEmail(((HashMap<String, Object>) data.get("stringData")).get("email").toString());
			} else {
				secretDtlVO.setEmail("");
			}

		} else if ("kubernetes.io/tls".equals(data.get("type"))) {
			String removeStr = "/iam/namespace/" + namespace + "/secret/" + secret + "/data/";

			secretDtlVO.setCrtFile(((HashMap<String, Object>) data.get("stringData")).get("tls.crt").toString()
					.replaceAll(removeStr, ""));
			secretDtlVO.setKeyFile(((HashMap<String, Object>) data.get("stringData")).get("tls.key").toString()
					.replaceAll(removeStr, ""));
			if ((HashMap<String, Object>) ((HashMap<String, Object>) data.get("metadata")).get("annotations") != null) {
				secretDtlVO.setLabel(
						((HashMap<String, Object>) ((HashMap<String, Object>) data.get("metadata")).get("annotations"))
								.get("cloudzcp.io/description").toString());
			} else {
				secretDtlVO.setLabel("");
			}

			String fileDir = UriComponentsBuilder.fromUriString("https://console.cloudzcp.io")
					.path("/iam/namespace/{namespace}/secret/{secret}/data/").buildAndExpand(namespace, secret)
					.toString();

			secretDtlVO.setCrtPath(fileDir + secretDtlVO.getCrtFile());
			secretDtlVO.setKeyPath(fileDir + secretDtlVO.getKeyFile());
		}

		return secretDtlVO;
	}

	public Map<String, Object> deleteSecret(String namespace, String secret) throws Exception {
		String param = "?namespace=" + namespace + "&secret=" + secret;

		String url = UriComponentsBuilder.fromUriString(iamBaseUrl)
				.path("/iam/namespace/{namespace}/secret/{secret}" + param).buildAndExpand(namespace, secret)
				.toString();

		ApiResponseVo response = new ApiResponseVo();
		Map<String, Object> resultData = null;
		
		try {
			HttpEntity<String> requestEntity = null;
			
			HttpHeaders headers = new HttpHeaders();
			headers.setAccept(Arrays.asList(new MediaType[] { MediaType.APPLICATION_JSON }));
			headers.setContentType(MediaType.APPLICATION_JSON);

			requestEntity = new HttpEntity<String>(headers);

			RestTemplate restTemplate = new RestTemplate();
			ResponseEntity<ApiResponseVo> entity = restTemplate.exchange(url, HttpMethod.DELETE, requestEntity,
					ApiResponseVo.class);
			
			if (entity != null && entity.getStatusCode() == HttpStatus.OK) {
				response.setCode(entity.getBody().getCode());
				response.setMsg(entity.getBody().getMsg());

				response.setData(entity.getBody().getData());
				resultData = response.getData();
			}
			
		} catch (RestClientException e) {
			e.printStackTrace();
		}

		if (!response.getCode().equals(ApiResult.SUCCESS.getCode())) {
			throw new Exception(response.getMsg());
		}
		
		return resultData;
	}

}
