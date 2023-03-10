package com.storesmanagementsystem.dealer.dao;

import java.time.LocalDate;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceUnit;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestBody;

import com.storesmanagementsystem.dealer.dto.DealerProductInfoBean;
import com.storesmanagementsystem.dealer.dto.OrderDetails;
import com.storesmanagementsystem.dealer.dto.ProductInfoBean;
import com.storesmanagementsystem.dealer.dto.UserInfoBean;

import lombok.extern.java.Log;

@Log
@Repository
public class DealerDAOImpl implements DealerDAO {

	@PersistenceUnit
	private EntityManagerFactory fact;

	boolean isPresent = false;

	public OrderDetails placeOrder(String userId, @RequestBody OrderDetails orderDetails) {
		EntityManager mgr = fact.createEntityManager();
		EntityTransaction tx = mgr.getTransaction();

		try {
			tx.begin();
			UserInfoBean bean = mgr.find(UserInfoBean.class, Integer.parseInt(userId));
			System.out.println(bean);
//			Iterator<DealerProductInfoBean> itr = dealer.getDealersProds().iterator();
			OrderDetails cameOrder = null;
//			Iterator<OrderDetails> oritr = dealer.getOrders().iterator();
//			if (oritr.hasNext()) {
//				cameOrder = oritr.next();
//			}
//			LocalDate date = LocalDate.now();
//			if (itr.hasNext()) {
//				DealerProductInfoBean product = itr.next();
			ProductInfoBean prod = mgr.find(ProductInfoBean.class, orderDetails.getProductId());
			prod.setQuantity(prod.getQuantity() - orderDetails.getQuantity());

			Iterator<DealerProductInfoBean> iterator = bean.getDealersProds().iterator();
			if (!iterator.hasNext()) {

				System.out.println(prod);
				DealerProductInfoBean dealerProds = new DealerProductInfoBean();
				Query nquery = mgr.createNativeQuery(
						"SELECT d.name FROM user_info d INNER JOIN product_info o ON d.userId = o.userId WHERE o.productId="
								+ prod.getProductId());
				String name = (String) nquery.getSingleResult();
				dealerProds.setManufacturerName(name);
				prod.setQuantity(prod.getQuantity() - orderDetails.getQuantity());
				dealerProds.setProductId(orderDetails.getProductId());
				dealerProds.setUser(bean);
				dealerProds.setQuantity(orderDetails.getProduct().getQuantity());
				dealerProds.setProductName(prod.getProductName());
				dealerProds.setImageUrl(prod.getImageUrl());
				bean.getDealersProds().add(dealerProds);
				isPresent = true;
			} else {
				while (iterator.hasNext()) {
					DealerProductInfoBean dprod = iterator.next();
					if (dprod.getProductId() == orderDetails.getProductId()) {
						isPresent = true;
						dprod.setQuantity(dprod.getQuantity() + orderDetails.getQuantity());
					}
				}
			}

			if (isPresent == false) {
				DealerProductInfoBean dealerProds = new DealerProductInfoBean();
				Query nquery = mgr.createNativeQuery(
						"SELECT d.name FROM user_info d INNER JOIN product_info o ON d.userId = o.userId WHERE o.productId="
								+ prod.getProductId());
				String name = (String) nquery.getSingleResult();
				dealerProds.setManufacturerName(name);
				prod.setQuantity(prod.getQuantity() - orderDetails.getQuantity());
				dealerProds.setProductId(orderDetails.getProductId());
				dealerProds.setUser(bean);
				dealerProds.setQuantity(orderDetails.getQuantity());
				dealerProds.setProductName(prod.getProductName());
				dealerProds.setImageUrl(prod.getImageUrl());
				bean.getDealersProds().add(dealerProds);
			}
			LocalDate date = LocalDate.now();
			OrderDetails order = new OrderDetails();
			order.setUser(bean);
			order.setDateOfOrder(date);
			order.setProductId(orderDetails.getProductId());
			order.setProductName(prod.getProductName());
			order.setPaymentType(orderDetails.getPaymentType());
			order.setStatus("Not yet Delivered");
			order.setAmount(prod.getProductCost() * orderDetails.getQuantity());
			order.setRole(bean.getRole());
			order.setQuantity(orderDetails.getQuantity());
			order.setDateOfDelivery(date.plusDays(2));

			bean.getOrders().add(order);

			System.out.println(bean);
			mgr.persist(prod);
			mgr.persist(bean);
			mgr.flush();
			tx.commit();

			String jpql = "select o from OrderDetails o order by o.orderId desc";
			Query query = mgr.createQuery(jpql, OrderDetails.class);
			OrderDetails placedOrder = (OrderDetails) query.setMaxResults(1).getSingleResult();

			mgr.close();
			return placedOrder;

		} catch (Exception e) {
			for (StackTraceElement ele : e.getStackTrace()) {
				log.info(ele.toString());
				return null;
			}
		}
		return null;
	}

	@Override
	public boolean setSellingPrice(DealerProductInfoBean dealer) {
		EntityManager mgr = fact.createEntityManager();
		EntityTransaction tx = mgr.getTransaction();
		try {
			tx.begin();
			DealerProductInfoBean bean = mgr.find(DealerProductInfoBean.class, dealer.getDealersProductId());
			bean.setSellingPrice(dealer.getSellingPrice());
			mgr.persist(bean);
			tx.commit();
			return true;
		} catch (Exception e) {
			for (StackTraceElement ele : e.getStackTrace()) {
				log.info(ele.toString());
				return false;
			}
		}
		return false;
	}

	@Override
	public DealerProductInfoBean getProduct(int id) {
		EntityManager mgr = fact.createEntityManager();
		try {
			DealerProductInfoBean bean = mgr.find(DealerProductInfoBean.class, id);
			if (bean != null)
				return bean;
			else {
				return null;
			}
		} catch (Exception e) {
			for (StackTraceElement ele : e.getStackTrace()) {
				log.info(ele.toString());
				return null;
			}
		}
		return null;
	}

	@Override
	public OrderDetails getPaymentDeatils(int orderId) {
		EntityManager mgr = fact.createEntityManager();
		OrderDetails order = mgr.find(OrderDetails.class, orderId);
		if (order != null) {
			return order;
		} else {
			return null;
		}
	}

	@Override
	public boolean checkNameAvailability(String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<DealerProductInfoBean> getAllProducts(int userId) {
		EntityManager mgr = fact.createEntityManager();
		try {
			@SuppressWarnings("unchecked")
			TypedQuery<DealerProductInfoBean> query = (TypedQuery<DealerProductInfoBean>) mgr.createNativeQuery(
					"select * from dealer_prods where dealerId=" + userId, DealerProductInfoBean.class);
			List<DealerProductInfoBean> prods = query.getResultList();
			if (prods != null)
				return prods;
			else
				return null;
		} catch (Exception e) {
			for (StackTraceElement ele : e.getStackTrace()) {
				log.info(ele.toString());
				return null;
			}
		}
		return null;
	}

	@Override
	public boolean setMinimumQuantity(DealerProductInfoBean dealer) {
		EntityManager mgr = fact.createEntityManager();
		EntityTransaction tx = mgr.getTransaction();
		try {
			tx.begin();
			DealerProductInfoBean bean = mgr.find(DealerProductInfoBean.class, dealer.getDealersProductId());
			bean.setMinimumQuantity(dealer.getMinimumQuantity());
			mgr.persist(bean);
			tx.commit();
			return true;
		} catch (Exception e) {
			for (StackTraceElement ele : e.getStackTrace()) {
				log.info(ele.toString());
				return false;
			}
		}
		return false;
	}

	@Override
	public List<ProductInfoBean> getProducts() {
		EntityManager mgr = fact.createEntityManager();
		String jpql = "select p from ProductInfoBean p";
		TypedQuery<ProductInfoBean> query = mgr.createQuery(jpql, ProductInfoBean.class);
		List<ProductInfoBean> list = query.getResultList();
		return list;
	}

	@Override
	public boolean update(DealerProductInfoBean dealer) {
		EntityManager mgr = fact.createEntityManager();
		EntityTransaction tx = mgr.getTransaction();
		try {
			tx.begin();
			DealerProductInfoBean bean = mgr.find(DealerProductInfoBean.class, dealer.getDealersProductId());
			bean.setImageUrl(dealer.getImageUrl());
			bean.setProductName(dealer.getProductName());
			bean.setMinimumQuantity(dealer.getMinimumQuantity());
			bean.setQuantity(dealer.getQuantity());
			bean.setSellingPrice(dealer.getSellingPrice());
			mgr.persist(bean);
			tx.commit();
			return true;
		} catch (Exception e) {
			for (StackTraceElement ele : e.getStackTrace()) {
				log.info(ele.toString());
				return false;
			}
		}
		return false;
	}

}
