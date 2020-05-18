package erp.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageSerializable;
import erp.domain.Detail;
import erp.service.DetailService;
import erp.util.MyException;
import erp.util.ResultInfo;
import erp.vo.req.DetailFilterVo;
import erp.vo.resp.DetailRespVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

/**
 * 收支明细
 */
@RestController
@Slf4j
@RequestMapping("/detail")
public class DetailController {
    @Autowired
    private DetailService detailService;

    @RequestMapping("/getAll")
    public ResultInfo getAll(DetailFilterVo vo, String duringDate, Integer pageNum, Integer pageSize) {
        try {
            // 分页
            PageHelper.startPage(pageNum, pageSize);

            //处理日期格式
            if (!StringUtils.isEmpty(duringDate)) {
                String[] dates = duringDate.split(" ~ ");
                if (dates.length == 2) {
                    //处理前日期
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM");
                    vo.setFrontDate(simpleDateFormat.parse(dates[0].trim()));
                    //处理后日期
                    Calendar calendar = simpleDateFormat.getCalendar();
                    calendar.setTime(simpleDateFormat.parse(dates[1].trim()));
                    calendar.add(Calendar.MONTH, 1);
                    calendar.add(Calendar.MILLISECOND, -1);
                    vo.setBackDate(calendar.getTime());
                }
            }
            List<DetailRespVo> detailRespVos = detailService.findAll(vo);
            PageSerializable<DetailRespVo> pageInfo = new PageSerializable<>(detailRespVos);
            return new ResultInfo(true, pageInfo);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("[method:getAll]" + e.getMessage());
            return new ResultInfo(false, e.getMessage());
        }
    }

    @RequestMapping("/findOne")
    public ResultInfo findOne(int id) {
        Detail detail = detailService.findOneById(id);
        return new ResultInfo(true, detail);
    }

    @RequestMapping("/add")
    public synchronized ResultInfo add(Detail form) {
        try {
            detailService.insert(form);
            return new ResultInfo(true, form.getId());
        } catch (Exception e) {
            log.error("[method:add]" + e.getMessage());
            e.printStackTrace();
            return new ResultInfo(false, "添加失败！请确认所有的栏目都已填写");
        }
    }

    @RequestMapping("/update")
    public synchronized ResultInfo update(Detail form) {
        try {
            detailService.update(form);
        } catch (MyException e) {
            log.error("[method:update]" + e.getMessage());
            return new ResultInfo(false, e.getMessage());
        } catch (Exception e) {
            log.error("[method:update]" + e.getMessage());
            return new ResultInfo(false, "修改失败!");
        }
        return new ResultInfo(true);
    }

    @RequestMapping("/delete")
    public synchronized ResultInfo delete(Detail form) {
        try {
            detailService.delete(form);
            return new ResultInfo(true);
        } catch (Throwable t) {
            log.error("[method:delete]" + t.getMessage());
            return new ResultInfo(false, "删除失败!");
        }
    }

    @RequestMapping("/updateBalance")
    public synchronized ResultInfo updateBalance() {
        try {
            detailService.updateAllBalance();
            return new ResultInfo(true);
        } catch (Throwable t) {
            log.error("[method:updateBalance]" + t.getMessage());
            return new ResultInfo(false, "更新结存失败!");
        }
    }

    @PostMapping("/addVouchers")
    public synchronized ResultInfo addVouchers(MultipartFile file, Integer id) {
        try {
            detailService.insertVoucher(file, id);
            return new ResultInfo(true);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResultInfo(false);
        }
    }

    @PostMapping("/deleteVoucher")
    public synchronized ResultInfo deleteVoucher(Integer voucherId) {
        try {
            detailService.deleteVoucher(voucherId);
            return new ResultInfo(true);
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
            return new ResultInfo(false, "删除凭证失败");
        }
    }

    @RequestMapping("/vouchers")
    public ResultInfo vouchers(Integer id) {
        return new ResultInfo(true, detailService.listVoucher(id));
    }

    @GetMapping("/voucher/{fileName}")
    public void voucher(@PathVariable String fileName, HttpServletResponse response) {
        try {
            detailService.listVoucherByUrl(fileName, response);
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }
}
