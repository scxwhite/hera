// -----------------------------------------------------------------------------------
/*
	jQuery simpleTimeline plugin v1.0
	Copyright 2016 eScience-Center, University of Tübingen
	http://escience.uni-tuebingen.de
	
	Licensed under the MIT License
	https://opensource.org/licenses/MIT
*/
// -----------------------------------------------------------------------------------

;(function($) {	
	/* -------------------------------------------------------------------------------
		data = 
		[ // array of layers (horizontal bar groups)
			[ // array of data elements
				{ // data element (bar)
					id: string,	// unique identifier					
					start: number, // start < end
					end: number, // end > start
					label: string, // optional (if empty, id is displayed)
					css: object, // optional, passed to jQuery css() method
					className: string // optional CSS class name(s)
				},
				... // optional: more data elements
			],
			... // optional: more layers
		];
		
		options = {	
			phases: [ // ordered array of phases (left to right, oldest to newest)
				{ // phase object
					start: number, // start point
					end: number, // end point
					indicatorsEvery: number, // interval of indicator lines
					share: number, // ]0;1] must sum up to 1 over all phases
					className: string // optional: CSS class name
				},					
				... // optionally more phases
			],
			boxMargin: number, // margin of the timeline box within the container
			barHeight: number, // height in pixels, at least 2 * borderWidth + fontSize
			fontSize: number, // font size in pixels
			borderWidth: number, // border width in pixels
			verticalBarSpacing: number, // spacing between bars in pixel
			minWidth: number, // min width of timeline box,
			headerHeight: number, // height of indicator labels in pixels
			formatHeader: function, // formatting of indicator labels
			toggle: { // optional, if false, then not possible
				collapseTooltip: string, // tooltip text for collapse symbol
				expandTooltip: string // tooltip text for expand symbol
			}
		}
	------------------------------------------------------------------------------- */
	
    $.fn.simpleTimeline = function(options, data) {		
		var self = this;
		this._prevWidth = -1;
		this._selectedBar = null;
		
		// -------------------------------------------------------------------------------
		// PUBLIC METHODS
		// -------------------------------------------------------------------------------
		
		// -------------------------------------------------------------------------------
		// call refreshTimeline() afterwards
		this.setTimelineData = function(data) {
		// -------------------------------------------------------------------------------
			this._timelineData = data;			
			var dataMap = {}; // an id-data map for faster access
			for(var i = data.length - 1; i >= 0 ; i--)
				for(var d = data[i], j = d.length - 1; j >= 0; j--)
					dataMap[d[j].id] = d[j];			
			this._dataMap = dataMap;
			return this;
		};
		
		// -------------------------------------------------------------------------------
		// call refreshTimeline() afterwards
		this.getTimelineData = function() {
		// -------------------------------------------------------------------------------
			return this._timelineData;
		};
		
		// -------------------------------------------------------------------------------
		// might require refreshTimeline() afterwards
		this.setTimelineOptions = function(options) {
		// -------------------------------------------------------------------------------
			this._timelineOptions = $.extend({			
				phases: [],
				boxMargin: 5,
				barHeight: 16,
				fontSize: 12,
				borderWidth: 2,
				verticalBarSpacing: 3,
				minWidth: 500,
				headerHeight: 20,
				formatHeader: function(v) { return v; },
				toggle: {
					collapseTooltip: 'Collapse Timeline',
					expandTooltip: 'Expand Timeline'
				}
			}, options);
			return this;
		}
		
		// -------------------------------------------------------------------------------
		this.allTimelineBars = function() {
		// -------------------------------------------------------------------------------
			return this._timelineBars;
		}
		
		// -------------------------------------------------------------------------------
		this.getTimelineBar = function(id) {
		// -------------------------------------------------------------------------------
			if(typeof this._timelineBars[id] === 'undefined')
				return null;
			
			return this._timelineBars[id];
		}
		
		// -------------------------------------------------------------------------------
		this.unselectTimelineBar = function() {
		// -------------------------------------------------------------------------------
			if(this._selectedBar) {
				this._selectedBar.removeClass('selected');
				this._selectedBar = null;
			}
			
			return this;			
		}
		
		// -------------------------------------------------------------------------------
		this.selectTimelineBar = function(bar) {
		// -------------------------------------------------------------------------------
			if(typeof bar === 'string')
				bar = this.getTimelineBar(bar); 
						
			if(this._selectedBar && this._selectedBar.data('id') === bar.data('id'))
				return this;
			
			this.unselectTimelineBar();
			this._selectedBar = this._timelineBars[bar.data('id')];
			this._selectedBar.addClass('selected');
			
			return this;
		}
		
		// -------------------------------------------------------------------------------
		this.refreshTimeline = function() {
		// -------------------------------------------------------------------------------
			this._storeTimelineBarVisibilities();

			this._timelineBars = {};
			this._timelineBox.empty();
			
			if(this._timelineData.length == 0)
				return this;
			
			var oldOverflow = this.suspendOverflow();

			this._drawTimelinePhases();	
			this._drawTimelineBars();
			
			this.resumeOverflow(oldOverflow);
			return this;
		}
		
		// -------------------------------------------------------------------------------
		this.suspendOverflow = function() {
		// -------------------------------------------------------------------------------
			var oldOverflow = this.css('overflow');
			if(oldOverflow != 'hidden')
				this.css({ overflow: 'hidden' });
			
			return oldOverflow;
		}
		
		// -------------------------------------------------------------------------------
		this.resumeOverflow = function(oldOverflow) {
		// -------------------------------------------------------------------------------
			if(oldOverflow != 'hidden')
				this.css({ overflow: oldOverflow });
			
			return this;
		}
		
		// -------------------------------------------------------------------------------
		this.bindPopup = function(id, html) {
		// -------------------------------------------------------------------------------			
			this._dataMap[id].popup_html = html;
			return this;
		}
		
		// -------------------------------------------------------------------------------
		this.closePopup = function() {
		// -------------------------------------------------------------------------------
			var cur_popup = $('.timeline-popup');
			if(cur_popup.length > 0) {
				var closing_bar = self.getTimelineBar(cur_popup.data('id'));
				self.trigger('timeline:popup-closing', [ cur_popup, closing_bar ]);
				cur_popup.remove();
				self.trigger('timeline:popup-closed', [ closing_bar ]);
			}
			return this;
		}
		
		// -------------------------------------------------------------------------------
		// INTERNAL METHODS
		// -------------------------------------------------------------------------------
		
		// -------------------------------------------------------------------------------
		this._initializeTimeline = function() {
		// -------------------------------------------------------------------------------
			this.empty()
			
			this._timelineBox = $('<div/>').addClass('timeline-box').css({ 
				margin: this._timelineOptions.boxMargin + 'px ' + this._timelineOptions.boxMargin + 'px' 
			});
			
			this.append(this._timelineBox);
			
			if(typeof this._timelineOptions.toggle === 'object') {
				// toggles are siblings of timeline container!
				this.parent().append($('<span/>')					
					.attr('id', 'timeline-expand')
					.attr('title', this._timelineOptions.toggle.expandTooltip)
					.on('click', self._onTimelineExpand)
					.hide()
				).append($('<span/>')					
					.attr('id', 'timeline-collapse')
					.attr('title', this._timelineOptions.toggle.collapseTooltip)
					.on('click', this._onTimelineCollapse)
					.show()
				);
			}
			
			this.deferredResize(this._onContainerResize, 300);
			this._onContainerResize();
		}
		
		// -------------------------------------------------------------------------------
		this._storeTimelineBarVisibilities = function() {
		// -------------------------------------------------------------------------------
			this._barVisibilities = {}; // current visibilities to restore after redraw
			for(var k in this._timelineBars)
				this._barVisibilities[k] = this._timelineBars[k].is(':visible');
		}
		
		// -------------------------------------------------------------------------------
		// repositions vertical bar positions if there are emtpy horizontal layers
		this.compactLayers = function() {
		// -------------------------------------------------------------------------------
			var oldOverflow = this.suspendOverflow();
			
			var empty_layers = 0;
			for(var l = 0; l < this._timelineData.length; l++) {
				var visible_bars = $('.timeline-bar[data-layer="' + l + '"]').filter(':visible');
				
				if(visible_bars.length == 0) {
					empty_layers++;
					continue;
				}
				
				visible_bars.css({
					top: this._timelineOptions.headerHeight + (l - empty_layers) * (this._timelineOptions.barHeight + this._timelineOptions.verticalBarSpacing)
				});
			}
			
			var adj_height = (this._timelineData.length - empty_layers) * (this._timelineOptions.barHeight + this._timelineOptions.verticalBarSpacing);
			
			$('.timeline-phase, .timeline-indicator-line').each(function() {
				$(this).css({ height: adj_height });
			});
			
			this._timelineBox.height(this._timelineOptions.headerHeight + adj_height - 1);
			
			this.resumeOverflow(oldOverflow);
		}
		
		// -------------------------------------------------------------------------------
		this._drawTimelineBars = function() {
		// -------------------------------------------------------------------------------	
			for(var l = 0; l < this._timelineData.length; l++) {				
				var layer = this._timelineData[l];
				
				for(var b = 0; b < layer.length; b++) {
					var datum = layer[b];
					
					var barDiv = $('<div/>')
						.attr('data-id', datum.id)
						.attr('data-layer', l)
						.addClass('timeline-bar')
						.html(typeof datum.label === 'undefined' ? datum.id : datum.label);
					
					if(this._barVisibilities.hasOwnProperty(datum.id) && this._barVisibilities[datum.id] === false)
						barDiv.hide();
					
					this._timelineBox.append(barDiv);
					
					// determine begin position
					var left_pos = -1;
					var right_pos = -1;
					
					for(var p = 0; p < this._timelineOptions.phases.length; p++) {
						var phase = this._timelineOptions.phases[p];
						
						if(left_pos == -1 
						   && datum.start >= phase.start 
						   && datum.start < phase.end) 
						{
							left_pos = this._phaseInfos[p].left + (datum.start - phase.start) * this._phaseInfos[p].width / (phase.end - phase.start);
						}
						
						if(left_pos >= 0
						   && datum.end > phase.start
						   && datum.end <= phase.end)
						{
							right_pos = this._phaseInfos[p].left + (datum.end - phase.start) * this._phaseInfos[p].width / (phase.end - phase.start);
							break;
						}
					}
					
					var lr_margin = 2;
					
					barDiv.css({
						left: left_pos + lr_margin,
						top: this._timelineOptions.headerHeight + l * (this._timelineOptions.barHeight + this._timelineOptions.verticalBarSpacing),
						height: this._timelineOptions.barHeight - 2 * this._timelineOptions.borderWidth,
						'line-height': this._timelineOptions.barHeight - 2 * this._timelineOptions.borderWidth + 'px',
						'font-size': this._timelineOptions.fontSize,
						'border-width': this._timelineOptions.borderWidth,
						width: right_pos - left_pos - 2 * this._timelineOptions.borderWidth - 2 * lr_margin + 1
					});
					
					if(typeof datum.css !== 'undefined')
						barDiv.css(datum.css);
					
					if(typeof datum.className !== 'undefined')
						barDiv.addClass(datum.className);
					
					this._timelineBars[datum.id] = barDiv;
					
					barDiv.on('click', function(e) {
						$(this).trigger('timeline:barclick', [ e ]);
					});
				}	
			}
			
			this.compactLayers();
		}
		
		// -------------------------------------------------------------------------------
		this._drawTimelinePhases = function() {
		// -------------------------------------------------------------------------------
			var cur_y = this._timelineOptions.headerHeight;
			var cur_x = 0;
			
			var header_font_size = 12;			
			var phase_height = this._timelineData.length * (this._timelineOptions.barHeight + this._timelineOptions.verticalBarSpacing);
			this._phaseInfos = [];
			
			for(var p = 0; p < this._timelineOptions.phases.length; p++) {
				var is_last_phase = (p == this._timelineOptions.phases.length - 1);
				var phase = this._timelineOptions.phases[p];
				var phase_width = this._timelineBox.width() * phase.share;
				
				var phaseInfo = {
					top: cur_y,
					left: cur_x,
					right: cur_x + phase_width,
					width: phase_width					
				};
				
				this._phaseInfos.push(phaseInfo);
				
				var phaseDiv = $('<div/>').addClass('timeline-phase').css({
					top: phaseInfo.top,
					left: phaseInfo.left,
					height: phase_height,
					width: phase_width
				});
				
				if(typeof phase.className === 'string')
					phaseDiv.addClass(phase.className);
								
				this._timelineBox.append(phaseDiv);
				
				// draw headers				
				var last_box_end_x = -1;
				
				for(var pos = phase.start; is_last_phase ? pos <= phase.end : pos < phase.end; pos += phase.indicatorsEvery) {
					var is_first = (p == 0 && pos == phase.start);
					
					// indicator label
					var titleBox = $("<span/>").addClass('timeline-phase-header').text(this._timelineOptions.formatHeader(pos));					
					this._timelineBox.append(titleBox);
					
					// calculate position of indicator
					var left_pos = phaseInfo.left + (pos - phase.start) * phase_width / (phase.end - phase.start);
					
					// draw indicator
					this._timelineBox.append(
						$('<div/>').addClass('timeline-indicator-line').css({		
							top: phaseInfo.top,
							left: left_pos,
							width: 0,
							height: phase_height
						})
					);
					
					// align label
					if(pos == phase.end)
						left_pos -= titleBox.outerWidth();
					else if(!is_first) {
						left_pos -= titleBox.outerWidth() / 2;
						
						// omit this label if it overlaps with previous one
						var excess = left_pos + titleBox.outerWidth() - this._timelineBox.width();
						if(excess > 0)
							left_pos -= excess;
					}
					
					// check for overlap with previous label
					if(left_pos < last_box_end_x) {
						titleBox.hide();
					}
					else {
						titleBox.css({ left: left_pos });						
						last_box_end_x = left_pos + titleBox.outerWidth();
					}
				}
				
				cur_x += phase_width;
			}
			
			this._timelineBox.height(this._timelineOptions.headerHeight + phase_height - 1);
		}
		
		// -------------------------------------------------------------------------------
		// EVENT HANDLERS
		// -------------------------------------------------------------------------------
		
		// -------------------------------------------------------------------------------
		this._onContainerResize = function() {
		// -------------------------------------------------------------------------------
			if(!self.is(':visible'))
				return;
			
			var newWidth = Math.max(self.width() - 2 * self._timelineOptions.boxMargin, self._timelineOptions.minWidth);			
			if(self._prevWidth == newWidth)
				return;
			
			self._prevWidth = newWidth;
			
			self._timelineBox.width(							
				 newWidth - (self._timelineOptions.toggle ? $('#timeline-collapse').outerWidth() : 0)
			);
			
			self.refreshTimeline();						
		}
		
		// -------------------------------------------------------------------------------
		this._onTimelineCollapse = function(e) {
		// -------------------------------------------------------------------------------
			self.fadeOut();
			$('#timeline-expand').fadeIn();
			$('#timeline-collapse').hide();
		}
		
		// -------------------------------------------------------------------------------
		this._onTimelineExpand = function(e) {
		// -------------------------------------------------------------------------------
			$('#timeline-expand').hide();
			$('#timeline-collapse').fadeIn();
			self.fadeIn();
			self._onContainerResize();
		}
		
		// -------------------------------------------------------------------------------
		this._onBarClick = function(e, orig_event) {
		// -------------------------------------------------------------------------------
			self.closePopup();			
			var bar = $(e.target);
			var clicked_data = self._dataMap[bar.data('id')];
			if(typeof clicked_data === 'undefined' || typeof clicked_data['popup_html'] === 'undefined' || clicked_data['popup_html'] === '') 
				return;
			
			// create popup window
			var popup = $('<div/>').addClass('timeline-popup').data('id', bar.data('id'));
			popup.append(
				$('<a/>').addClass('timeline-popup-close-button').attr('href', 'javascript:void(0)').text('\u00d7').click(function() {
					self.closePopup();
				})
			).append(
				$('<div/>').addClass('timeline-popup-content-wrapper').append(
					$('<div/>').addClass('timeline-popup-content').html(clicked_data.popup_html)
				)
			).append(
				$('<div/>').addClass('timeline-popup-tip-container').append(
					$('<div/>').addClass('timeline-popup-tip')
				)
			);
			
			// show and position at mouse pointer
			$('body').append(popup);
			var pos = {				
      			top: orig_event.pageY - popup.outerHeight() + 5, 
      			left: orig_event.pageX - popup.outerWidth() / 2 // + 3
			};
			
			if(pos.left < 0) {
				popup.find('.timeline-popup-tip-container').css({ 'margin-left': orig_event.pageX - 19 });
				pos.left = 0;
			}
			
			if(pos.top < 0) {
				popup.find('.timeline-popup-tip-container').hide();
				pos.top = 0;
			}
			
			popup.css(pos);
			
			// trigger an event to celebrate the opened popup
			self.trigger('timeline:popup-open', [
				orig_event, // 1st extra param: original jQuery click event
				popup, 		// 2nd extra param: the popup div object
				bar 		// 3rd extra param: the bar div object that was clicked
			]);
		}
				
		// -------------------------------------------------------------------------------
		// INITIALIZATION		
		// -------------------------------------------------------------------------------
		
		this.setTimelineOptions(options);
		this.setTimelineData(data);
		this._initializeTimeline();
		this.on('timeline:barclick', this._onBarClick);		
		
		return this; 
    }; 
}(jQuery));