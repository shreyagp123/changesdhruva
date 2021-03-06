package com.cisco.dhruva;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** The Dhruva microservice. */
@RestController
@RequestMapping("${cisco-spark.server.api-path:/api}/v1")
public class DhruvaController {
  private final Logger log = LoggerFactory.getLogger(this.getClass());

  @Autowired private Environment env;

  @Autowired
  public DhruvaController() {}
}
