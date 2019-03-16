package com.slife.controller;

import com.slife.base.entity.ReturnDTO;
import com.slife.base.vo.DataTable;
import com.slife.enums.HttpCodeEnum;
import com.slife.exception.SlifeException;
import com.slife.service.ProcessService;
import com.slife.utils.ReturnDTOUtil;
import com.slife.dto.ProcessDefDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.util.List;

/**
 * @author: felixu
 * @createTime: 2017/12/11.
 */
@Controller
@RequestMapping("/process")
@Api(value = "流程管理的前端控制器", tags = "流程管理接口", description = "流程相关")
public class ProcessController {

	@Autowired
	ProcessService processService;

	@ApiOperation(value = "进入流程管理页面", notes = "进入流程管理页面")
	@GetMapping(value = "")
	public String list(org.springframework.ui.Model model, HttpServletRequest request) {
		model.addAttribute("url", request.getContextPath()+"/process/");
		return "process/processDefinitionList";
	}

    @ApiOperation(value = "获取流程列表数据", notes = "获取流程列表数据:使用约定的DataTable")
    @PostMapping(value = "/list")
    @ResponseBody
    public DataTable<ProcessDefDTO> list(@RequestBody DataTable dt) {
        if (null == dt) {
            return null;
        }
        return processService.getProcessByPage(dt);
    }

    @ApiOperation(value = "读取xml/image资源", notes = "resType为xml或者image")
    @GetMapping(value = "/resource")
    public void resourceRead(String procDefId, String proInsId, String resType, HttpServletResponse response) {
        if ("xml".equals(resType)) {
            response.setContentType("application/xml");
        } else {
            response.setContentType("image/png");
        }

        try {
            InputStream resourceAsStream = processService.resourceRead(procDefId, proInsId, resType);
            byte[] b = new byte[1024];
            int len = -1;
            while ((len = resourceAsStream.read(b, 0, 1024)) != -1) {
                response.getOutputStream().write(b, 0, len);
            }
        } catch (Exception e) {
            throw new SlifeException(HttpCodeEnum.FAIL);
        }
    }

    @ApiOperation(value = "激活/挂起", notes = "active为激活，suspend为挂起")
    @PostMapping("/status")
    @ResponseBody
    public ReturnDTO updateState(@RequestParam String status, @RequestParam String procDefId) {
        processService.updateStatus(status, procDefId);
        return ReturnDTOUtil.success();
    }

    @ApiOperation(value = "批量删除已部署流程", notes = "批量删除已部署流程")
    @PostMapping("/delete")
    @ResponseBody
    public ReturnDTO deleteProcIns(@RequestParam("ids") List<String> ids) {
	    if (CollectionUtils.isEmpty(ids)) {
	        return ReturnDTOUtil.custom(HttpCodeEnum.INVALID_REQUEST);
        }
	    ids.forEach(id -> processService.deleteDeployment(id));
        return ReturnDTOUtil.success();
    }
}
