/*
 * Created on Aug 15, 2013
 *
 * Copyright (C) 2013 by Viettel Network Company. All rights reserved
 */
package com.viettel.it.persistence;

import com.viettel.controller.AppException;
import com.viettel.controller.SysException;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Định nghĩa các hàm chung thao tác với database.
 * 
 * @param <T>
 *            Model class
 * @param <PK>
 *            Kiểu dữ liệu của khóa chính
 * @author Nguyễn Hải Hà (hanh45@viettel.com.vn)
 * @since Aug 15, 2013
 * @version 1.0.0
 */
public interface GenericDao<T, PK extends Serializable> {

	/**
	 * Lấy thông tin đối tượng theo khóa chính.
	 * <p>
	 * Có 3 định dạng PK đã được hỗ trợ như sau:
	 * <ul>
	 * <li>Integer</li>
	 * <li>String</li>
	 * <li>Composite</li>
	 * </ul>
	 * 
	 * @param id
	 *            (PK) khóa chính của bảng.
	 * @return 
	 * <li>Đối tượng lấy được theo id truyền vào.</li>
	 * <li>Giá trị <b>null</b> nếu không tồn tại.</li>
	 * @throws AppException
	 * @throws SysException
	 */
	public T findById(PK id) throws AppException, SysException;

	/**
	 * Xóa thông tin một đối tượng.
	 * 
	 * @param object
	 *            đối tượng muốn xóa.
	 * @throws AppException
	 * @throws SysException
	 */
	public void delete(T object) throws AppException, SysException;

	/**
	 * Xóa thông tin một danh sách các đối tượng.
	 * 
	 * @param objects
	 *            danh sách các đối tượng muốn xóa.
	 * @throws AppException
	 * @throws SysException
	 */
	public void delete(List<T> objects) throws AppException, SysException;

	/**
	 * Thêm mới một đối tượng.
	 * 
	 * @param object
	 *            đối tượng cần thêm mới.
	 *            
	 * @return khóa chính.
	 * 
	 * @throws AppException
	 * @throws SysException
	 */
	public PK save(T object) throws AppException, SysException;

	/**
	 * Thêm mới hoặc cập nhật thông tin một đối tượng.
	 * 
	 * @param object
	 *            đối tượng cần thêm mới hoặc cập nhật thông tin.
	 *            
	 * @throws AppException
	 * @throws SysException
	 */
	public void saveOrUpdate(T object) throws AppException, SysException;

	/**
	 * Thêm mới hoặc cập nhật thông tin một danh sách các đối tượng.
	 * 
	 * @param objects
	 *            danh sách các đối tượng.
	 *            
	 * @throws AppException
	 * @throws SysException
	 */
	public void saveOrUpdate(List<T> objects) throws AppException, SysException;

	/**
	 * Lấy danh sách tất cả các đối tượng.
	 * 
	 * @return danh sách tất cả các đối tượng.
	 * 
	 * @throws AppException
	 * @throws SysException
	 */
	public List<T> findList() throws AppException, SysException;

	/**
	 * <p>
	 * Lấy thông tin tất cả các đối tượng.
	 * <p>
	 * Hỗ trợ <b>Filter</b> theo trường mong muốn.
	 * <p>
	 * Hỗ trợ <b>Order</b> theo trường mong muốn.
	 * 
	 * @param filters
	 *            the filters
	 * @param orders
	 *            the orders
	 *            
	 * @return Danh sách các đối tượng.
	 * 
	 * @throws AppException
	 * @throws SysException
	 */
	public List<T> findList(Map<String, Object> filters,
                            Map<String, String> orders) throws AppException, SysException;

	/**
	 * Lấy danh sách các đối tượng có phân trang.
	 *
	 * @param first
	 *            Số thứ tự của phần tử đầu tiên cần lấy.
	 * @param pageSize
	 *            Số phần tử muốn lấy.
	 * @param filters
	 *            Danh sách các trường cần filter.
	 * @return Danh sách tất cả các đối tượng trong database theo trang
	 *         (paging).
	 *
	 * @throws AppException
	 * @throws SysException
	 */
	public List<T> findList(int first, int pageSize, Map<String, Object> filters)
			throws AppException, SysException;

	/**
	 * <p>
	 * Lấy danh sách các đối tượng có phân trang.
	 * <p>
	 * Hỗ trợ filter, order.
	 *
	 * @param first
	 *            Số thứ tự của phần tử đầu tiên cần lấy.
	 * @param pageSize
	 *            Số phần tử muốn lấy.
	 * @param filters
	 *            Danh sách các trường cần filter.
	 * @param orders
	 *            Danh sách các trường cần order.
	 * @return Danh sách tất cả các đối tượng trong database theo trang
	 *         (paging).
	 *
	 * @throws AppException
	 * @throws SysException
	 */
	public List<T> findList(int first, int pageSize,
                            Map<String, Object> filters, Map<String, String> orders)
			throws AppException, SysException;

	/**
	 * Lấy tổng số các phần tử theo filter.
	 * 
	 * @param filters
	 *            Danh sách các trường cần filter.
	 *            
	 * @return Tổng số phàn tử.
	 * 
	 * @throws AppException
	 * @throws SysException
	 */
	public int count(Map<String, Object> filters) throws AppException,
            SysException;

	/**
	 * Lấy tổng số phần tử.
	 * 
	 * @return Tổng số phần tử
	 * 
	 * @throws AppException
	 * @throws SysException
	 */
	public int count() throws AppException, SysException;
	
	/**
	 * Persist object
	 * 
	 * @param object
	 * @throws AppException
	 * @throws SysException
	 */
	public void persist(T object) throws AppException, SysException;
	
	/**
	 * Merge object
	 * 
	 * @param object
	 * @throws AppException
	 * @throws SysException
	 */
	public void merge(T object) throws AppException, SysException;
	
	/**
	 * Get object by id
	 * 
	 * @param id
	 * @throws AppException
	 * @throws SysException
	 */
	public T get(PK id) throws AppException, SysException;
}
