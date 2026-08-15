package com.reviewer.common.pageInfo;
import lombok.Value; 

@Value
public class PageInfo {
	private int listCount;
	private int currentPage;
	private int boardLimit;
	private int pageLimit;
	private int maxPage;
	private int startPage; 
	private int endPage;
	private int offset;
	
	private PageInfo(int listCount, int currentPage, int boardLimit, int pageLimit) {
		if(currentPage < 1) {
			currentPage = 1;
		}
		this.listCount = listCount; 
		this.currentPage = currentPage;
		this.boardLimit = boardLimit;
		this.pageLimit = pageLimit;
		this.maxPage = (int)Math.ceil((double)listCount / boardLimit);
		this.startPage = (currentPage - 1) / pageLimit * pageLimit + 1;
		this.endPage = startPage + pageLimit - 1;
		this.offset = (currentPage - 1) * boardLimit;
	}
	
	public static PageInfo of(int listCount, int currentPage, int boardLimit, int pageLimit) {
		return new PageInfo(listCount, currentPage, boardLimit, pageLimit);
	}
}



