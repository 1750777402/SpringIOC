package com.ygz.dao.impl;

import org.ygz.framework.ioc.annoation.Repository;

import com.ygz.dao.PaperDAO;

@Repository
public class PaperDAOImpl implements PaperDAO {
	
	public PaperDAOImpl() {
		System.out.println("dao instance");
	}

	@Override
	public void addPaper() {
		System.out.println("添加试卷");
	}

}
