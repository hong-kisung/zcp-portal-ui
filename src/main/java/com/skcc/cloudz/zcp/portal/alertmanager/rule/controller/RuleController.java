package com.skcc.cloudz.zcp.portal.alertmanager.rule.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = RuleController.RESOURCE_PATH)
public class RuleController {

	private static final Logger log = LoggerFactory.getLogger(RuleController.class);

	static final String RESOURCE_PATH = "/alertmanager";

	@GetMapping(value = "/rule", consumes = MediaType.ALL_VALUE, produces = MediaType.TEXT_HTML_VALUE)
	public String ruleList(Model model) throws Exception {
		return "content/alertmanager/rule/rules";
	}

}
