$(function() {

	/* 基本 =================================================================== */

		/* body タグにデバイス識別クラスを付加 */

			function addDeviceClass(width)	{

				$('body').removeClass('IE6-8').removeClass('IE9').removeClass('SMP').removeClass('TAB').removeClass('PC');

				if( !$.support.opacity ) {																								/* Internet Explorer 6-8 判定 */
					$('body').addClass('IE6-8').addClass('PC');
				} else if ( !('text-shadow' in document.createElement('div').style) ) {		/* Internet Explorer 9 判定 */
					$('body').addClass('IE9').addClass('PC');
				} else {
					if ( width < 600 ) {
						$('body').addClass('SMP');
					} else if ( width < 960 ) {
						$('body').addClass('TAB');
					} else {
						$("body").addClass('PC');
					}
				}

			}

			$(window).on('load resize', function() {
				addDeviceClass($(window).width());
			});

});
