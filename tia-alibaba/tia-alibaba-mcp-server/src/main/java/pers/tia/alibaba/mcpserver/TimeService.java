/**
 * Copyright (c) 2025. Shanghai HEADING information Engineering Co., Ltd. All rights reserved.
 */
package pers.tia.alibaba.mcpserver;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author ZhengYu
 * @since 1.0
 */
@Slf4j
@Service
public class TimeService {

  @Tool(description = "Get the time of a specified city.")
  public String getZoneTime(
      @ToolParam(description = "Time zone id, such as Asia/Shanghai") String timeZoneId) {
    log.info("input: {}", timeZoneId);
    return getTimeByZoneId(timeZoneId);
  }

  private String getTimeByZoneId(String zoneId) {
    ZoneId zid = ZoneId.of(zoneId);
    ZonedDateTime zonedDateTime = ZonedDateTime.now(zid);
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");
    return zonedDateTime.format(formatter);
  }
}