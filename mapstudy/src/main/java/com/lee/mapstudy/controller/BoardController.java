package com.lee.mapstudy.controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FileUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import com.lee.mapstudy.boardDao.MemberDao;
import com.lee.mapstudy.boardDto.PagingContentDto;
import com.lee.mapstudy.boardDto.PagingDto;
import com.lee.mapstudy.service.BoardService;
import com.lee.mapstudy.service.MemberService;
import com.lee.mapstudy.service.ReplyService;
import com.lee.mapstudy.service.RreplyService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class BoardController {
	
	private final MemberService memberService;
	private final BoardService boardService;
	private final ReplyService replyService;
	private final RreplyService rreplyService;
	private final MemberDao memeDao;
	
	//????????? ??????
	@GetMapping("/login")
	public String login(HttpServletResponse res, Principal principal) throws IOException {
		
		if(principal != null) {
			res.sendRedirect("/board");
		}
		
		System.out.println("login");
		return "/tiles/view/auth/login";
	}
	//????????????
//	@GetMapping("/logout")
//	public String logout(HttpSession session) {
//		session.invalidate();
//		System.out.println("logout");
//		return "/tiles/view/auth/login";
//	}
	//???????????? ??????
	@GetMapping("/join")
	public String join(HttpServletResponse res, Principal principal) throws IOException {
		
		if(principal != null) {
			res.sendRedirect("/board");
		}
		
		System.out.println("join");
		return "/tiles/view/auth/join";
	}
	//????????? ????????????
	@PostMapping("/checkId")
	@ResponseBody
	public int checkId(@RequestBody Map<String, Object> param) {
		System.out.println("checkId");
		return memberService.checkId((String)param.get("id"));
	}
	//???????????? ?????? ???
	@PostMapping("/joinMember")
	@ResponseBody
	public Map<String, Object> joinMember(@RequestBody Map<String, Object> params) throws Exception {
		System.out.println("joinMember");
		System.out.println(params);
		
		return memberService.insertMember(params);
	}
	//?????? ?????? ??????
	@GetMapping("/myInfo")
	public String myInfo(HttpSession session, Model model) {
		System.out.println("myInfo");
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("id", session.getAttribute("userId"));
		model.addAttribute("myInfo", memeDao.userInfo(params));
		return "/tiles/view/auth/myInfo";
	}
	//?????? ?????? ??????
	@PostMapping("/updateM")
	@ResponseBody
	public Map<String, Object> updateMemberInfo(@RequestBody Map<String, Object> param, HttpSession session){
		System.out.println("updateM");
		Map<String, Object> params = new HashMap<String, Object>();
		params.putAll(param);
		params.put("id", session.getAttribute("userId"));
		return memberService.updateMemberInfo(params);
	}
	//?????? ???????????? ??????
	@PostMapping("/updateP")
	@ResponseBody
	public Map<String, Object> updateMemberPwd(@RequestBody Map<String, Object> param, HttpSession session){
		System.out.println("updateP");
		Map<String, Object> params = new HashMap<String, Object>();
		params.putAll(param);
		params.put("id", session.getAttribute("userId"));
		return memberService.updateMemberPwd(params);
	}
	
	//////////////////////////////////////////////////////////
	
	//????????? & ?????????
	@GetMapping("/board")
	public String board(Model model) throws Exception {
		System.out.println("board");
		model.addAttribute("cateList", boardService.categoryList());
		
		return "/tiles/view/board/board";
	}
	//?????????
	@GetMapping("/boardAjax/{pageNum}")
	public String boardAjax(@PathVariable int pageNum, @RequestParam Map<String, Object> params, PagingContentDto pcd, Model model) throws Exception {
		System.out.println("board");
		//????????? ??????
		PagingDto paging = new PagingDto();
		int boardListCnt = 0;

		if("2".equals(params.get("optionVal"))) {
			// ?????? ??? ??????
	        boardListCnt = boardService.ReplyListCnt(params);
	        
	        pcd.setPage(pageNum);
	        
	        paging.setPcd(pcd);
	        paging.setTotalCount(boardListCnt);  
	        
	        model.addAttribute("paging", paging);
	        model.addAttribute("page", pcd.getPage());
			model.addAttribute("list", boardService.replySearchList(params, pcd));
		}
		if("1".equals(params.get("optionVal"))) {
			// ?????? ??? ??????
	        boardListCnt = boardService.boardListCnt(params);
	        
	        pcd.setPage(pageNum);
	        
	        paging.setPcd(pcd);
	        paging.setTotalCount(boardListCnt);  
	        model.addAttribute("paging", paging);
	        model.addAttribute("page", pcd.getPage());
			model.addAttribute("list", boardService.boardList(params, pcd));
		}
		return "/tiles/ajax/ajax/ajax-board";
	}
	//???????????? ????????????
	@PostMapping("/insertCate")
	@ResponseBody
	public int insertCate(@RequestBody Map<String, Object> params) {
		return boardService.insertCate(params);
	}
	//???????????? ??????
	@PostMapping("/deleteCate")
	@ResponseBody
	public int deleteCate(@RequestBody Map<String, Object> params) {
		return boardService.deleteCate(params); 
	}
	
	//??? ????????????
	@GetMapping("/boardWrite")
	public String boardWrite(Model model) {
		System.out.println("board");
		model.addAttribute("cateList", boardService.categoryList());
		return "/tiles/view/board/boardWrite";
	}
	//??? ??????
	@PostMapping("/insertB")
	@ResponseBody
	public Map<String, Object> insertB(@RequestBody Map<String, Object> params) {
		System.out.println("insertB");
		System.out.println(params);
		Map<String, Object> boardWright = new HashMap<String, Object>();
		boardWright.putAll(params);
		boardWright.put("text" ,((String)params.get("bcontent")).replaceAll("<([^>]+)>", ""));
		
		//????????? ?????????
		return boardService.insertBoard(boardWright);
	}
	//?????? ?????????
	@PostMapping(value="/uploadSummernoteImageFile", produces = "application/json")
	@ResponseBody
	public Map<String, Object> uploadSummernoteImageFile(@RequestParam("file") MultipartFile multipartFile) {
		Map<String, Object> result = new HashMap<String, Object>();
		Map<String, Object> fileInput = new HashMap<String, Object>();
		String fileRoot = "C:\\summernote_image\\";	//????????? ?????? ?????? ??????
		String originalFileName = multipartFile.getOriginalFilename();	//???????????? ?????????
		String extension = originalFileName.substring(originalFileName.lastIndexOf(".")); //?????? ?????????
				
		String savedFileName = UUID.randomUUID() + extension; //????????? ?????? ???
		
		File targetFile = new File(fileRoot + savedFileName);	
		
		fileInput.put("fileroot", fileRoot);
		fileInput.put("sfname", savedFileName);
		fileInput.put("ofname", originalFileName);
		fileInput.put("extension", extension);
		
		
		try {
			String fileSeq = boardService.insertFile(fileInput);
			InputStream fileStream = multipartFile.getInputStream();
			FileUtils.copyInputStreamToFile(fileStream, targetFile); //?????? ??????
			result.put("url", "/thubnail/"+fileSeq);
			result.put("responseCode", "success");
		} catch (IOException e) {
			FileUtils.deleteQuietly(targetFile); //????????? ?????? ??????
			result.put("responseCode", "error");
			e.printStackTrace();
		}
		return result;
	}
	//???????????? ????????????
	@GetMapping("/download/{fnum}")
	public ResponseEntity<Resource> downloadAttach(@PathVariable String fnum) throws MalformedURLException {
		 //...itemId ???????????? ????????? ???????????? ?????? ????????? uploadFileName??? ?????? ???????????? ???????????? ?????? ????????? storeFileName??? ???????????? ????????? ??????
	    Map<String, Object> fileInfo = boardService.downloadFile(fnum);
	    String filePath = String.valueOf(fileInfo.get("fileroot"));
	    String serverFileName = String.valueOf(fileInfo.get("sfilename"));
	    String originalName = String.valueOf(fileInfo.get("ofilename"));
	    
	    
	    UrlResource resource = new UrlResource("file:" + filePath + serverFileName);
	    
	    //?????? ?????? ???????????? ?????? ????????? ?????? ?????? ??? ????????? ????????? ?????? ?????????
	    String encodedUploadFileName = UriUtils.encode(originalName,
	    StandardCharsets.UTF_8);
	    
	    //?????? ????????? ResponseHeader??? ???????????? ??????. ????????? ????????? ????????? ??? ????????? ??????.
	    //????????? ????????????.
	    String contentDisposition = "attachment; filename=\"" + encodedUploadFileName + "\"";
	    
	    return ResponseEntity.ok()
	 			.header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
			 	.body(resource);
	}
	//???????????? ??????
	@PostMapping("/deleteAttachFile")
	@ResponseBody
	public Map<String, Object> deleteAttachFile(@RequestBody Map<String, Object> param) {
		return boardService.deleteAttachFile(param);
	}
	
	//????????? url ??????
	@GetMapping("/thubnail/{fileseq}")
	public ResponseEntity<Resource> thubnail(@PathVariable("fileseq") String seq) {
		Map<String, Object> fileCheck = boardService.fileCheck(seq);
		
		String path = "";
		String filename = "";
		if(fileCheck!=null) {
			path = String.valueOf(fileCheck.get("fileroot"));
			filename = String.valueOf(fileCheck.get("sfilename"));
		}
		
		Resource resource = new FileSystemResource(path + filename);
		if(!resource.exists()) 
			return new ResponseEntity<Resource>(HttpStatus.NOT_FOUND);
			HttpHeaders header = new HttpHeaders();
			Path filePath = null;
		try{
			filePath = Paths.get(path + filename);
			header.add("Content-type", Files.probeContentType(filePath));
		}catch(IOException e) {
			e.printStackTrace();
		}
		return new ResponseEntity<Resource>(resource, header, HttpStatus.OK);
	}
	
	//??? ????????????
	@GetMapping("/boardContent/{bnum}")
	public String boardContent(@PathVariable("bnum") String bnum, Model model, HttpSession session) {
		System.out.println("board");
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("id", session.getAttribute("userId"));
		model.addAttribute("myInfo", memeDao.userInfo(params));
		model.addAttribute("boardInfo", boardService.selectBoardInfo(bnum));
		model.addAttribute("fileInfo", boardService.attachFileList(bnum));
		
		return "/tiles/view/board/boardContent";
	}
	//??? ??????
	@PostMapping("/deleteB")
	@ResponseBody
	public Map<String, Object> deleteB(@RequestBody Map<String, Object> param) {
		System.out.println("deleteB");
		return boardService.deleteBoard(param);
	}
	//??? ?????? ?????????
	@GetMapping("/boardEdit/{bnum}")
	public String boardEdit(@PathVariable("bnum") String bnum, Model model) {
		System.out.println("boardEdit");
		model.addAttribute("editList", boardService.selectBoardInfo(bnum));
		model.addAttribute("cateList", boardService.categoryList());
		return "/tiles/view/board/boardEdit";
	}
	//??? ??????
	@PostMapping("/updateB")
	@ResponseBody
	public Map<String, Object> updateB(@RequestBody Map<String, Object> params) {
		System.out.println("updateB");
		Map<String, Object> boardWright = new HashMap<String, Object>();
		boardWright.putAll(params);
		boardWright.put("text" ,((String)params.get("bcontent")).replaceAll("<([^>]+)>", ""));
		return boardService.updateBoard(boardWright);
	}
	
	////////////////////////////////////////////////////////////////////////////////////
	//?????? ??????
	@PostMapping("/replyInput")
	@ResponseBody
	public String replyInsert(@RequestBody Map<String, Object> params, HttpSession session){
		params.put("mid", session.getAttribute("userId"));
		replyService.insertReply(params);
		
		String bnum = (String) params.get("bnum");
		return bnum;
	}
	//????????????
	@PostMapping("/updateReply")
	@ResponseBody
	public int updateReply(@RequestBody Map<String, Object> params) {
		return replyService.updateReply(params);
	}
	//?????? ?????? ??????
	@PostMapping("/deleteReplyA")
	@ResponseBody
	public int deleteReplyA(@RequestBody Map<String, Object> params) {
		return replyService.deleteReplyA(params);
	}
	//?????? ?????? ??????
	@PostMapping("/deleteReply")
	@ResponseBody
	public int deleteReply(@RequestBody Map<String, Object> params) {
		return replyService.deleteReply(params);
	}
	
	//?????? ?????????
	@GetMapping("/replyAjax/{bnum}")
	public String replyAjax(@PathVariable("bnum") String bnum, Model model, HttpSession session) {
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("userId", session.getAttribute("userId"));
		param.put("bnum", bnum);
		model.addAttribute("rList", replyService.selectReply(param));
		model.addAttribute("userNick", rreplyService.selectMember(param));
		model.addAttribute("rrList", rreplyService.selectRreply(param));
		return "/tiles/ajax/ajax/ajax-reply";
	}
	/////////////////////////////////////////////////////////////////////////
	//????????? ??????
	@PostMapping("/rreplyInput")
	@ResponseBody
	public int rreplyInsert(@RequestBody Map<String, Object> params, HttpSession session) {
		params.put("mid", session.getAttribute("userId"));
		return rreplyService.insertRreply(params);
	}
	//????????? ??????&??????
	@PostMapping("/updateOrDeleteRR")
	@ResponseBody
	public int updateOrDeleteRR(@RequestBody Map<String, Object> params) {
		return rreplyService.updateOrDeleteRR(params);
	}
}
