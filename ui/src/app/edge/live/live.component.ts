import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Edge, Service, Utils, Widgets, EdgeConfig } from '../../shared/shared';

@Component({
  selector: 'live',
  templateUrl: './live.component.html'
})
export class LiveComponent implements OnInit {

  public edge: Edge = null
  public config: EdgeConfig = null;
  public widgets: Widgets = null;

  constructor(
    private route: ActivatedRoute,
    protected utils: Utils,
    private service: Service,
  ) {
  }

  ngOnInit() {
    this.service.setCurrentComponent('', this.route).then(edge => {
      this.edge = edge;
    });
    this.service.getConfig().then(config => {
      this.config = config;
      this.widgets = config.widgets;
    })
  }
}