package com.ygz.service.impl;

import org.ygz.framework.ioc.annoation.Autowired;
import org.ygz.framework.ioc.annoation.Service;

import com.ygz.dao.PaperDAO;
import com.ygz.service.PaperService;

@Service("service")
public class PaperServiceImpl implements PaperService {
	
	@Autowired
	private PaperDAO dao;
	
	public PaperServiceImpl() {
		System.out.println("service instance");
	}

	public PaperServiceImpl(PaperDAO dao) {
		this.dao = dao;
	}

	public void setDao(PaperDAO dao) {
		this.dao = dao;
	}
	
	@Override
	public void addPaper() {
		dao.addPaper();
	}
}
