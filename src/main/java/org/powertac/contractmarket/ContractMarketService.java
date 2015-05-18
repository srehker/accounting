package org.powertac.contractmarket;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.joda.time.Instant;
import org.powertac.common.Competition;
import org.powertac.common.TimeService;
import org.powertac.common.interfaces.Accounting;
import org.powertac.common.interfaces.BrokerProxy;
import org.powertac.common.interfaces.CapacityControl;
import org.powertac.common.interfaces.ContractMarket;
import org.powertac.common.interfaces.ContractNegotiationMessageListener;
import org.powertac.common.interfaces.InitializationService;
import org.powertac.common.interfaces.ServerConfiguration;
import org.powertac.common.interfaces.TimeslotPhaseProcessor;
import org.powertac.common.msg.ContractAccept;
import org.powertac.common.msg.ContractAnnounce;
import org.powertac.common.msg.ContractConfirm;
import org.powertac.common.msg.ContractDecommit;
import org.powertac.common.msg.ContractEnd;
import org.powertac.common.msg.ContractOffer;
import org.powertac.common.repo.BrokerRepo;
import org.powertac.common.repo.RandomSeedRepo;
import org.powertac.common.repo.TariffRepo;
import org.powertac.common.repo.TariffSubscriptionRepo;
import org.powertac.common.repo.TimeslotRepo;
import org.powertac.common.spring.SpringApplicationContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ContractMarketService extends TimeslotPhaseProcessor implements
		InitializationService, ContractMarket {

	static private Logger log = Logger.getLogger(ContractMarketService.class
			.getSimpleName());

	@Autowired
	private TimeService timeService;

	@Autowired
	private Accounting accountingService;

	@Autowired
	private CapacityControl capacityControlService;

	@Autowired
	private BrokerProxy brokerProxyService;

	@Autowired
	private BrokerRepo brokerRepo;

	@Autowired
	private TimeslotRepo timeslotRepo;

	@Autowired
	private TariffRepo tariffRepo;

	@Autowired
	private TariffSubscriptionRepo tariffSubscriptionRepo;

	@Autowired
	private ServerConfiguration serverProps;

	@Autowired
	private RandomSeedRepo randomSeedService;

	private Set<ContractNegotiationMessageListener> registrations = new HashSet<ContractNegotiationMessageListener>();

	@Override
	public void setDefaults() {
		// TODO Auto-generated method stub

	}

	@Override
	public String initialize(Competition competition,
			List<String> completedInits) {
		int index = completedInits.indexOf("AccountingService");
		if (index == -1) {
			return null;
		}

		for (Class<?> messageType : Arrays.asList(ContractOffer.class,
				ContractAccept.class, ContractAnnounce.class,
				ContractConfirm.class, ContractDecommit.class,
				ContractEnd.class)) {
			brokerProxyService.registerBrokerMessageListener(this, messageType);
		}
		registrations.clear();

		super.init();

		serverProps.configureMe(this);

		// Register the NewTariffListeners
		List<ContractNegotiationMessageListener> listeners = SpringApplicationContext
				.listBeansOfType(ContractNegotiationMessageListener.class);
		for (ContractNegotiationMessageListener listener : listeners) {
			registerContractNegotiationMessageListener(listener);
		}

		serverProps.publishConfiguration(this);
		return "ContractMarket";
	}

	@Override
	public void activate(Instant time, int phaseNumber) {
		// TODO Auto-generated method stub

	}

	@Override
	public void forwardCollectedMessages() {
		// TODO Auto-generated method stub

	}

	@Override
	public void registerContractNegotiationMessageListener(
			ContractNegotiationMessageListener listener) {
		registrations.add(listener);

	}

}
