package database;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.Query;

import exception.EntryAlreadyExistsException;
import exception.EntryNotExistsException;

public class LinkConnector {

	private static final String PERSISTENCE_UNIT_NAME = "workers";
	private static EntityManagerFactory factory;
	public static EntityManager entityManager;
	
	public static Date currentDate() {
		return Date.valueOf(LocalDate.now());
	}
    
    public static void connect() {
		factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
		entityManager = factory.createEntityManager();
	}

	public static boolean isConnected() {
		return entityManager.isOpen();
	}

	public static void close() {
		entityManager.close();
	}

	/**
	 * добавить работника в базу
	 * @param login логин (совпадает с ldap)
	 * @param name полное имя
	 * @param isAdmin права администратора
	 * @throws EntryAlreadyExistsException если пользователь с таким логином уже существует
	 */
	public static void addWorker(String login, String name, boolean isAdmin) throws EntryAlreadyExistsException {
		Worker worker = getWorker(login);
		if (worker != null) throw new EntryAlreadyExistsException("User with login=\'" + login + "\' already exists");
		entityManager.getTransaction().begin();
		worker = new Worker();
		worker.setLogin(login);
		worker.setAdmin(isAdmin);
		worker.setFlag(Worker.Flags.NONE);
		worker.setName(name);
		entityManager.persist(worker);
		entityManager.getTransaction().commit();
	}

	/**
	 * @param id работника в базе
	 * @return объект работника, null если работника нет в базе
	 */
	public static Worker getWorker(int id) {
		Worker worker = entityManager.find(Worker.class, id);
		return worker;
	}

	/**
	 * 
	 * @param login работника в ldap
	 * @return объект работника, null если работника нет в базе
	 */
	public static Worker getWorker(String login) {
		Query q = entityManager.createQuery("SELECT w FROM Worker w WHERE w.login = \'" + login + "\'");
		try {
			return (Worker) q.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}
	
	/**
	 * добавить метку о явке работника в заданный день
	 * @param workerId id работника в базе
	 * @param day дата
	 * @param hours количество часов
	 * @param flag флаг
	 * @throws EntryNotExistsException если работника нет в базе
	 */
	public static void addWorktime(int workerId, LocalDate day, short hours, short flag) throws EntryNotExistsException {
		Worker worker = getWorker(workerId);
		if (worker == null) throw new EntryNotExistsException("No worker found with id=" + workerId);
		entityManager.getTransaction().begin();
		Worktime worktime = new Worktime();
		worktime.setDay(Date.valueOf(day));
		worktime.setFlag(flag);
		worktime.setHours(hours);
		worktime.setWorker(worker);
		entityManager.persist(worktime);
		entityManager.getTransaction().commit();
	}

	/**
	 * отметить явку работника в сегодняшний день
	 * @param id работника в базе
	 * @throws EntryNotExistsException если работника нет в базе
	 */
	public static void logWorkerIn(int id) throws EntryNotExistsException {
		if (getWorktime(id, LocalDate.now()) == null) // ещё не отмечался
			addWorktime(id, LocalDate.now(), (short) 8, Worker.Flags.NONE);
	}

	/**
	 * добавить сообщение администратору
	 * @param senderId id отправителя
	 * @param text текст сообщения
	 * @throws EntryNotExistsException если отправителя нет в базе
	 */
	public static void addMessage(int senderId, String text) throws EntryNotExistsException {
		Worker sender = getWorker(senderId);
		if (sender == null) throw new EntryNotExistsException("No worker found with id = " + senderId);
		entityManager.getTransaction().begin();
		Message message = new Message();
		message.setDay(currentDate());
		message.setMessage(text);
		message.setSender(sender);
		message.setStatus(Message.Status.UNREAD);
		entityManager.persist(message);
		entityManager.getTransaction().commit();
	}
	
	/**
	 * 
	 * @param id отметки о явке
	 * @return объект отметки о явке, null если отметки не было
	 */
	public static Worktime getWorktime(WorktimeKey id) {
		Worktime worktime = entityManager.find(Worktime.class, id);
		return worktime;
	}
	
	/**
	 * @param workerId id работника в базе
	 * @param day дата
	 * @return отметка о явке работника в указанный день, null если отметки не было
	 * @throws EntryNotExistsException если работника не существует
	 */
	public static Worktime getWorktime(int workerId, LocalDate day) throws EntryNotExistsException {
		Worker worker = getWorker(workerId);
		if (worker == null) throw new EntryNotExistsException("No worker found with id=" + workerId);
		WorktimeKey worktimeKey = new WorktimeKey(workerId);
		worktimeKey.setDay(Date.valueOf(day));
		return getWorktime(worktimeKey);
	}

	/**
	 * установить количество часов, отработанных работником в данный день (даже если отметки не было)
	 * @param workerId id работника
	 * @param day дата
	 * @param workHours часы работы
	 * @throws EntryNotExistsException если работника нет в базе
	 */
	public static void updateWorktimeHours(int workerId, LocalDate day, short workHours) throws EntryNotExistsException {
		updateWorktimeHours(workerId, day, workHours, Worker.Flags.NONE);
	}
	
	/**
	 * установить количество часов, отработанных работником, и флаг в данный день (даже если отметки не было)
	 * @param workerId id работника
	 * @param day дата
	 * @param workHours часы работы
	 * @param flag состояние работника
	 * @throws EntryNotExistsException если работника нет в базе
	 */
	public static void updateWorktimeHours(int workerId, LocalDate day, short workHours, short flag) throws EntryNotExistsException {
		Worker worker = getWorker(workerId);
		if (worker == null) throw new EntryNotExistsException("No worker found with id=" + workerId);
		Worktime worktime = getWorktime(workerId, day);
		if (worktime == null) // �� ���������
			addWorktime(workerId, day, workHours, Worker.Flags.NONE);
		else { // ���������
			entityManager.getTransaction().begin();
			worktime.setHours(workHours);
			worktime.setFlag(flag);
			entityManager.persist(worktime);
			entityManager.getTransaction().commit();
		}
	}

	/**
	 * выставить состояние работнику (можно отложенный)
	 * @param id работника
	 * @param flag состояние
	 * @param startDate дата начала действия флага, null если с сегодняшнего дня
	 */
	public static void updateWorkerFlag(int id, short flag, LocalDate startDate) {
		Worker worker = getWorker(id);
		entityManager.getTransaction().begin();
		worker.setFlag(flag);
		if (startDate != null)
			worker.setFlagStartDate(Date.valueOf(startDate));
		entityManager.persist(worker);
		entityManager.getTransaction().commit();
	}

	/** 
	 * сбросить флаг работника
	 * @param id работника
	 */
	public static void clearWorkerFlag(int id) {
		updateWorkerFlag(id, Worker.Flags.NONE, null);
	}

	/**
	 * @return список всех работников в базе
	 */
	@SuppressWarnings("unchecked")
	public static List<Worker> getWorkers() {
		Query q = entityManager.createQuery("SELECT w FROM Worker w");
		return q.getResultList();
	}
	
	/**
	 * @return количество работников в базе
	 */
	public static int getWorkersCount() {
		Query q = entityManager.createQuery("SELECT COUNT(1) FROM Worker w");
		return ((Long) q.getSingleResult()).intValue();
	}
	
	/**
	 * @return список всех работников в базе, сортированный по именам
	 */
	@SuppressWarnings("unchecked")
	public static List<Worker> getWorkersSortedByName() {
		Query q = entityManager.createQuery("SELECT w FROM Worker w ORDER BY w.name");
		return q.getResultList();
	}
	
	/**
	 * @return список всех меток о явках
	 */
	@SuppressWarnings("unchecked")
	public static List<Worktime> getWorktimes() {
		Query q = entityManager.createQuery("SELECT w FROM Worktime w");
		return q.getResultList();
	}
	
	/**
	 * @return список всех меток о явках в указанный период, сортированный по дате
	 */
	@SuppressWarnings("unchecked")
	public static List<Worktime> getWorktimes(LocalDate from, LocalDate to) {
		Query q = entityManager.createQuery("SELECT w FROM Worktime w WHERE w.day < \'" 
				+ to.plusDays(1).toString() + "\' AND w.day > \'" + from.minusDays(1).toString() + "\' ORDER BY w.day");
		return q.getResultList();
	}
	
	/**
	 * @return список всех меток о явках указанного работника в указанный период
	 */
	public static List<Worktime> getWorktimes(int workerId, LocalDate from, LocalDate to) {
		List<Worktime> wts = getWorktimes(from, to);
		List<Worktime> res = new ArrayList<Worktime>();
		for (Worktime wt : wts)
			if (wt.getWorker().getId() == workerId)
				res.add(wt);
		return res;
	}
	
	/**
	 * @param messageStatus состояние сообщения (прочитано/нет)
	 * @return список всех сообщений с заданным состоянием
	 */
	@SuppressWarnings("unchecked")
	public static List<Message> getMessages(short messageStatus) {
		Query q = entityManager.createQuery("SELECT m FROM Message m WHERE m.status = " + Short.toString(messageStatus));
		return q.getResultList();
	}
	
	/**
	 * @param messageStatus состояние сообщения (прочитано/нет)
	 * @return список всех сообщений с заданным состоянием
	 */
	@SuppressWarnings("unchecked")
	public static List<Message> getMessagesSortedByDay(short messageStatus) {
		Query q = entityManager.createQuery("SELECT m FROM Message m WHERE m.status = " + Short.toString(messageStatus) + " ORDER BY m.day");
		return q.getResultList();
	}
	
	/**
	 * @param from начало периода
	 * @param to конец периода
	 * @return карта, где работнику соответствует массив отметок о его явках
	 */
	public static WorkerToWorktimesTable getWorkerToWorktimes(LocalDate from, LocalDate to) {
		List<Worker> workers = getWorkers();
		WorkerToWorktimesTable wwt = new WorkerToWorktimesTable(from, to);
		for (Worker worker : workers) {
			List<Worktime> wt = getWorktimes(worker.getId(), from, to);
			if (wt == null)
				wwt.put(worker, new Worktime[0]);
			else
				wwt.put(worker, wt.toArray(new Worktime[wt.size()]));
		}
		return wwt;
	}
	
	/**
	 * Выставить работнику права администратора
	 * @param workerId
	 * @param isAdmin
	 */
	public static void updateWorkerAdmin(int workerId, boolean isAdmin) {
		Worker worker = getWorker(workerId);
		if (isAdmin == false && worker.isAdmin() && getAdminCount() < 2) return; // нельзя забрать права у последнего админа
		entityManager.getTransaction().begin();
		worker.setAdmin(isAdmin);
		entityManager.persist(worker);
		entityManager.getTransaction().commit();
	}
	
	/**
	 * изменить статус сообщения на прочитано/не прочитано
	 * @param msgId
	 * @param status
	 */
	public static void updateMessageStatus(int msgId, short status) {
		Message msg = getMessage(msgId);
		entityManager.getTransaction().begin();
		msg.setStatus(status);
		entityManager.persist(msg);
		entityManager.getTransaction().commit();
	}

	/** 
	 * @param id
	 * @return объект сообщения, null если такого нет в базе
	 */
	private static Message getMessage(int id) {
		Message msg = entityManager.find(Message.class, id);
		return msg;
	}
	
	/**
	 * @param messagestatus статус сообщения (прочитано/нет)
	 * @return количество сообщений с данным статусом
	 */
	public static int getMessagesCount(short messagestatus) {
		Query q = entityManager.createQuery("SELECT COUNT(1) FROM Message m WHERE m.status = " + Short.toString(messagestatus));
		return ((Long) q.getSingleResult()).intValue();
	}

	/**
	 * Изменить имя работника
	 * @param id работника
	 * @param name новое имя
	 * @throws EntryNotExistsException если работник не найден в базе
	 */
	public static void updateWorkerName(int id, String name) throws EntryNotExistsException {
		Worker worker = getWorker(id);
		if (worker == null) throw new EntryNotExistsException("No worker found with id = " + id);
		entityManager.getTransaction().begin();
		worker.setName(name);
		entityManager.persist(worker);
		entityManager.getTransaction().commit();
	}
	
	/**
	 * @return количество работников с парвами администратора
	 */
	public static int getAdminCount() {
		Query q = entityManager.createQuery("SELECT COUNT(1) FROM Worker w WHERE w.isAdmin <> 0");
		return ((Long) q.getSingleResult()).intValue();
	}

}
