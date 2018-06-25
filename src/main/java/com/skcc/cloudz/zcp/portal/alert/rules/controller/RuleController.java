package com.skcc.cloudz.zcp.portal.alert.rules.controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.skcc.cloudz.zcp.common.constants.ApiResult;
import com.skcc.cloudz.zcp.portal.alert.rules.service.RuleService;
import com.skcc.cloudz.zcp.portal.alert.rules.vo.RuleVo;
import com.skcc.cloudz.zcp.portal.management.user.vo.UserVo;

@Controller
@RequestMapping(value = RuleController.RESOURCE_PATH)
public class RuleController {

	static final String RESOURCE_PATH = "/alert";

	@Autowired
	private RuleService ruleService;

	@GetMapping(value = "/rules", consumes = MediaType.ALL_VALUE, produces = MediaType.TEXT_HTML_VALUE)
	public String rules() throws Exception {
		return "content/alert/rules/rules";
	}

	@GetMapping(value = "/ruleList", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public Map<String, Object> ruleList() throws Exception {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			resultMap.put("resultCd", ApiResult.SUCCESS.getCode());
			resultMap.put("resultMsg", ApiResult.SUCCESS.getName());
			resultMap.put("resultData", ruleService.getRuleList());
		} catch (Exception e) {
			e.printStackTrace();

			resultMap.put("resultCd", ApiResult.FAIL.getCode());
			resultMap.put("resultMsg", e.getMessage());
		}

		return resultMap;
	}

	@GetMapping(value = "/addRule", consumes = MediaType.ALL_VALUE, produces = MediaType.TEXT_HTML_VALUE)
	public String addRule(Model model) throws Exception {
		return "content/alert/rules/addrules";
	}

	@GetMapping(value = "/detailRule", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public String detailRule(Model model) throws Exception {
		return "content/alert/rules/detailrules";
	}

	@PostMapping(value = "/deleteRule", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public Map<String, Object> deleteRule(@RequestBody RuleVo ruleVo) throws Exception {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			ruleService.deleteRule(ruleVo);

			resultMap.put("resultCd", ApiResult.SUCCESS.getCode());
			resultMap.put("resultMsg", ApiResult.SUCCESS.getName());
		} catch (Exception e) {
			e.printStackTrace();

			resultMap.put("resultCd", ApiResult.FAIL.getCode());
			resultMap.put("resultMsg", e.getMessage());
		}

		return resultMap;
	}

}
