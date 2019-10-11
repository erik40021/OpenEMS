import { Component, Input, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { IonReorderGroup, ModalController } from '@ionic/angular';
import { TranslateService } from '@ngx-translate/core';
import { Edge, EdgeConfig, Service, Websocket } from 'src/app/shared/shared';

type ChargeMode = 'FORCE_CHARGE' | 'EXCESS_POWER';
type Priority = 'CAR' | 'STORAGE';

@Component({
    selector: 'evcsCluster-modal',
    templateUrl: './evcsCluster-modal.page.html'
})
export class ModalComponentEvcsCluster implements OnInit {

    @Input() public edge: Edge;
    @Input() public config: EdgeConfig.Component = null;
    @Input() public componentId: string;
    @Input() public evcsMap: { [sourceId: string]: EdgeConfig.Component } = {};

    @ViewChild(IonReorderGroup, { static: true })
    public reorderGroup: IonReorderGroup;

    public chargeState: ChargeState;
    private chargePlug: ChargePlug;
    public evcsAmount: number;
    public swiperIndex: number = 0;
    public slideOpts = {
        noSwiping: true,
        noSwipingClass: 'swiper-no-swiping',
        //noSwipingSelector: 'ion-range, ion-toggle',
        initialSlide: 0,
        speed: 1000,
    };
    public firstEvcs: string;
    public lastEvcs: string;
    public prioritizedEvcsList: string[];

    constructor(
        protected service: Service,
        public websocket: Websocket,
        public router: Router,
        private route: ActivatedRoute,
        protected translate: TranslateService,
        private modalCtrl: ModalController
    ) {
    }

    ngOnInit() {
        this.prioritizedEvcsList = this.config.properties["evcs.ids"];
        this.evcsAmount = this.prioritizedEvcsList.length;
        this.lastEvcs = this.prioritizedEvcsList[this.evcsAmount - 1]
        this.firstEvcs = this.prioritizedEvcsList[0];
    }

    doReorder(ev: any) {
        let oldListOrder = this.prioritizedEvcsList;
        this.prioritizedEvcsList = ev.detail.complete(this.prioritizedEvcsList);

        let newListOrder = this.prioritizedEvcsList;

        if (this.edge != null) {
            this.edge.updateComponentConfig(this.websocket, this.config.id, [
                { name: 'evcs.ids', value: newListOrder }
            ]).then(response => {
                this.config.properties.chargeMode = newListOrder;
            }).catch(reason => {
                this.config.properties.chargeMode = oldListOrder;
                console.warn(reason);
            });
        }
    }

    cancel() {
        this.modalCtrl.dismiss();
    }

    /**  
    * Updates the Charge-Mode of the EVCS-Controller.
    * 
    * @param event 
    */
    updateChargeMode(event: CustomEvent, currentController: EdgeConfig.Component) {
        let oldChargeMode = currentController.properties.chargeMode;
        let newChargeMode: ChargeMode;

        switch (event.detail.value) {
            case 'FORCE_CHARGE':
                newChargeMode = 'FORCE_CHARGE';
                break;
            case 'EXCESS_POWER':
                newChargeMode = 'EXCESS_POWER';
                break;
        }

        if (this.edge != null) {
            this.edge.updateComponentConfig(this.websocket, currentController.id, [
                { name: 'chargeMode', value: newChargeMode }
            ]).then(response => {
                currentController.properties.chargeMode = newChargeMode;
            }).catch(reason => {
                currentController.properties.chargeMode = oldChargeMode;
                console.warn(reason);
            });
        }
    }
    /**
     * Changed the Priority between the components of the charging session
     */
    priorityChanged(event: CustomEvent, currentController: EdgeConfig.Component) {
        let oldPriority = currentController.properties.priority;
        let newPriority: Priority;

        switch (event.detail.value) {
            case 'CAR':
                newPriority = 'CAR';
                break;
            case 'STORAGE':
                newPriority = 'STORAGE';
                break;
        }

        if (this.edge != null) {
            this.edge.updateComponentConfig(this.websocket, currentController.id, [
                { name: 'priority', value: newPriority }
            ]).then(response => {
                currentController.properties.priority = newPriority;
            }).catch(reason => {
                currentController.properties.priority = oldPriority;
                console.warn(reason);
            });
        }
    }

    /**
     * Updates the Min-Power of force charging
     *
     * @param event
     */
    updateForceMinPower(event: CustomEvent, currentController: EdgeConfig.Component) {
        let oldMinChargePower = currentController.properties.forceChargeMinPower;
        let newMinChargePower = event.detail.value;

        if (this.edge != null) {
            this.edge.updateComponentConfig(this.websocket, currentController.id, [
                { name: 'forceChargeMinPower', value: newMinChargePower }
            ]).then(response => {
                currentController.properties.forceChargeMinPower = newMinChargePower;
            }).catch(reason => {
                currentController.properties.forceChargeMinPower = oldMinChargePower;
                console.warn(reason);
            });
        }
    }

    /**
     * Updates the Min-Power of default charging
     *
     * @param event
     */
    updateDefaultMinPower(event: CustomEvent, currentController: EdgeConfig.Component) {
        let oldMinChargePower = currentController.properties.defaultChargeMinPower;
        let newMinChargePower = event.detail.value;

        if (this.edge != null) {
            this.edge.updateComponentConfig(this.websocket, currentController.id, [
                { name: 'defaultChargeMinPower', value: newMinChargePower }
            ]).then(response => {
                currentController.properties.defaultChargeMinPower = newMinChargePower;
            }).catch(reason => {
                currentController.properties.defaultChargeMinPower = oldMinChargePower;
                console.warn(reason);
            });
        }
    }

    currentLimitChanged(event: CustomEvent, property: String) {

    }

    /**
     * uptdate the state of the toggle whitch renders the minimum charge power
     * 
     * @param event 
     * @param phases 
     */
    allowMinimumChargePower(event: CustomEvent, phases: number, currentController: EdgeConfig.Component) {

        let oldMinChargePower = currentController.properties.defaultChargeMinPower;

        let newMinChargePower = 0;
        if (oldMinChargePower == null || oldMinChargePower == 0) {
            newMinChargePower = phases != undefined ? 1400 * phases : 4200;
        }
        if (this.edge != null) {
            this.edge.updateComponentConfig(this.websocket, currentController.id, [
                { name: 'defaultChargeMinPower', value: newMinChargePower }
            ]).then(response => {
                currentController.properties.defaultChargeMinPower = newMinChargePower;
            }).catch(reason => {
                currentController.properties.defaultChargeMinPower = oldMinChargePower;
                console.warn(reason);
            });
        }
    }

    /**
    * Aktivates or deaktivates the Charging
    * 
    * @param event 
    */
    enableOrDisableCharging(event: CustomEvent, currentController: EdgeConfig.Component) {

        let oldChargingState = currentController.properties.enabledCharging;
        let newChargingState = !oldChargingState;
        if (this.edge != null) {
            this.edge.updateComponentConfig(this.websocket, currentController.id, [
                { name: 'enabledCharging', value: newChargingState }
            ]).then(response => {
                currentController.properties.enabledCharging = newChargingState;
            }).catch(reason => {
                currentController.properties.enabledCharging = oldChargingState;
                console.warn(reason);
            });
        }
    }
    /**
     * Gets the output for the current state or the current charging power
     * 
     * @param power 
     * @param state 
     * @param plug 
     */
    getState(power: Number, state: number, plug: number, currentController: EdgeConfig.Component) {
        if (currentController.properties.enabledCharging != null && currentController.properties.enabledCharging == false) {
            return this.translate.instant('Edge.Index.Widgets.EVCS.ChargingStationDeactivated');
        }

        if (power == null || power == 0) {

            this.chargeState = state;
            this.chargePlug = plug;

            if (this.chargePlug == null) {
                return this.translate.instant('Edge.Index.Widgets.EVCS.NotCharging');
            } else if (this.chargePlug != ChargePlug.PLUGGED_ON_EVCS_AND_ON_EV_AND_LOCKED) {
                return this.translate.instant('Edge.Index.Widgets.EVCS.CableNotConnected');
            }

            switch (this.chargeState) {
                case ChargeState.STARTING:
                    return this.translate.instant('Edge.Index.Widgets.EVCS.Starting');
                case ChargeState.UNDEFINED:
                case ChargeState.ERROR:
                    return this.translate.instant('Edge.Index.Widgets.EVCS.Error');
                case ChargeState.READY_FOR_CHARGING:
                    return this.translate.instant('Edge.Index.Widgets.EVCS.CarFull');
                case ChargeState.NOT_READY_FOR_CHARGING:
                    return this.translate.instant('Edge.Index.Widgets.EVCS.NotReadyForCharging');
                case ChargeState.AUTHORIZATION_REJECTED:
                    return this.translate.instant('Edge.Index.Widgets.EVCS.NotCharging');
            }
        }
        return this.translate.instant('Edge.Index.Widgets.EVCS.Charging');
    }

    /**
     * Round to 100 and 
     * Round up (ceil)
     * 
     * @param i 
     */
    formatNumber(i: number) {
        let round = Math.ceil(i / 100) * 100;
        return round;
    }

    /**
     * Get Value or 3
     * 
     * @param i 
     */
    getValueOrThree(i: number) {
        if (i == null || i == undefined) {
            return 3;
        } else {
            return i;
        }
    }

    //TODO: Do it in the edge component
    currentChargingPower(): number {
        return this.sumOfChannel("ChargePower");
    }

    private sumOfChannel(channel: String): number {

        let sum = 0;/*
    this.evcsMap.forEach(station => {
      let channelValue = this.edge.currentData.value.channel[station.id + "/" + channel];
      if (channelValue != null) {
        sum += channelValue;
      };
    });
    */
        return sum;
    }
}

enum ChargeState {
    UNDEFINED = -1,           //Undefined
    STARTING,                 //Starting
    NOT_READY_FOR_CHARGING,   //Not ready for Charging e.g. unplugged, X1 or "ena" not enabled, RFID not enabled,...
    READY_FOR_CHARGING,       //Ready for Charging waiting for EV charging request
    CHARGING,                 //Charging
    ERROR,                    //Error
    AUTHORIZATION_REJECTED    //Authorization rejected
}

enum ChargePlug {
    UNDEFINED = -1,                           //Undefined
    UNPLUGGED,                                //Unplugged
    PLUGGED_ON_EVCS,                          //Plugged on EVCS
    PLUGGED_ON_EVCS_AND_LOCKED = 3,           //Plugged on EVCS and locked
    PLUGGED_ON_EVCS_AND_ON_EV = 5,            //Plugged on EVCS and on EV
    PLUGGED_ON_EVCS_AND_ON_EV_AND_LOCKED = 7  //Plugged on EVCS and on EV and locked
}
