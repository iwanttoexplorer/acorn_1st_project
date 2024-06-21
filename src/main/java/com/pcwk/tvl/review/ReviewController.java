package com.pcwk.tvl.review;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.pcwk.ehr.cmn.ControllerV;
import com.pcwk.ehr.cmn.JView;
import com.pcwk.ehr.cmn.PLog;
import com.pcwk.ehr.cmn.StringUtil;
import com.pcwk.tvl.comment.CommentDTO;
import com.pcwk.tvl.like.LikeDTO;
import com.pcwk.ehr.cmn.SearchDTO;

/**
 * Servlet implementation class ReviewController
 */
@WebServlet("/review/review.do")
public class ReviewController extends HttpServlet implements ControllerV, PLog {
    private static final long serialVersionUID = 1L;

    private ReviewService reviewService;

    public ReviewController() {
        log.debug("---------------------");
        log.debug("ReviewController()");
        log.debug("---------------------");
        reviewService = new ReviewService(); // 인스턴스 생성
    }
    
    public JView doLikeSave(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	log.debug("---------------------");
        log.debug("doLikeSave()");
        log.debug("---------------------");
        String userId = StringUtil.nvl(request.getParameter("userId"), "");
        String aboardSeq = StringUtil.nvl(request.getParameter("aboardSeq"),"");
        log.debug("userId: {}",userId);
        log.debug("aboardSeq: {}",aboardSeq);
        LikeDTO like = new LikeDTO();
        like.setUserId(userId);
        like.setAboardSeq(Integer.parseInt(aboardSeq));
        int flag = reviewService.doLikeSave(like);
        log.debug("save flag:{}", flag);
        response.setContentType("UTF-8");
        response.setContentType("application/json");
       
    	return null;
    }
    public JView doLikeCount(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	log.debug("---------------------");
        log.debug("doLikeCount()");
        log.debug("---------------------");
        String aboardSeq = StringUtil.nvl(request.getParameter("aboardSeq"),"");
        log.debug("aboardSeq: {}",aboardSeq);
        LikeDTO like = new LikeDTO();
        like.setAboardSeq(Integer.parseInt(aboardSeq));
        int flag = reviewService.doLike(like);
        log.debug("Count flag:{}", flag);
        response.setContentType("UTF-8");
        response.setContentType("application/json");
        
    	return null;
    }
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doWork(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doWork(request, response);
    }

    public JView doRetrieve(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        log.debug("---------------------");
        log.debug("doRetrieve()");
        log.debug("---------------------");

        SearchDTO inVO = new SearchDTO();
        
        // 페이지 번호와 페이지 크기 파라미터 추가
        int pageNumber = Integer.parseInt(request.getParameter("pageNumber"));
        int pageSize = Integer.parseInt(request.getParameter("pageSize"));

        ReviewDao reviewDAO = new ReviewDao();
        try {
            // 페이징된 리뷰 목록과 전체 리뷰 수 가져오기
            List<ReviewDTO> list = reviewDAO.getReviews(pageNumber, pageSize);
            int totalReviews = reviewDAO.getTotalReviews();

            // 기존 코드 유지
            int i = 0;
            for (ReviewDTO vo : list) {
                log.debug("i: {}, vo: {}", i++, vo);
            }
            log.debug("list: " + list);

            // JSON 응답 준비
            Map<String, Object> result = new HashMap<>();
            result.put("reviews", list);
            result.put("totalReviews", totalReviews);

            Gson gson = new Gson();
            String json = gson.toJson(result);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(json);
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        return null;
    }

    public JView saveReview(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        log.debug("---------------------");
        log.debug("saveReview()");
        log.debug("---------------------");

        ReviewDTO inVO = new ReviewDTO();
        String contentId = StringUtil.nvl(request.getParameter("contentid"), "");
        String userId = StringUtil.nvl(request.getParameter("user_id"), "");
        String imgLink = StringUtil.nvl(request.getParameter("img_link"), "");
        String comments = StringUtil.nvl(request.getParameter("comments"), "");
        String title = StringUtil.nvl(request.getParameter("title"), "");

        inVO.setContentId(contentId);
        inVO.setUserId(userId);
        inVO.setImgLink(imgLink);
        inVO.setComments(comments);
        inVO.setTitle(title);

        int flag = reviewService.doSave(inVO);
        log.debug("flag : {}", flag);

        if (flag == 1) {
            log.debug("Review saved successfully.");
        } else {
            log.debug("Failed to save review.");
        }

        response.sendRedirect("review.do?work_div=doRetrieve"); // 저장 후 리뷰 게시판으로 리다이렉트
        return null; // JSP 페이지로 포워딩하지 않기 때문에 null을 반환
    }

    /*
    public JView doRetrieveDetail(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        log.debug("---------------------");
        log.debug("doRetrieveDetail()");
        log.debug("---------------------");

        String aboardSeq = StringUtil.nvl(request.getParameter("aboardSeq"), "");
        ReviewDTO inVO = new ReviewDTO();
        inVO.setAboardSeq(aboardSeq);

        ReviewDTO outVO = reviewService.doSelectOne(inVO);
        request.setAttribute("review", outVO);

        JView viewName = new JView("/SEOUL_TRAVEL/review/review_detail.jsp");
        return viewName;
    }
    */

    @Override
    public JView doWork(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        log.debug("---------------------");
        log.debug("doWork()");
        log.debug("---------------------");

        JView viewName = null;

        String workDiv = StringUtil.nvl(request.getParameter("work_div"), "");
        log.debug("workDiv : {}", workDiv);

        switch (workDiv) {
            case "doRetrieve":
                viewName = doRetrieve(request, response);
                break;
            case "saveReview":
                viewName = saveReview(request, response);
                break;
            case "doLikeSave":
                viewName = doLikeSave(request, response);
                break;
            case "doLikeCount":
                viewName = doLikeCount(request, response);
                break;
                /*
            case "doRetrieveDetail":
                viewName = doRetrieveDetail(request, response);
                break;
                */
            default:
                log.debug("ReviewController work_div를 확인하세요. : {}", workDiv);
                break;
        }

        if (viewName != null) {
            viewName.render(request, response);
        }
        
        return viewName;
    }
    
}
