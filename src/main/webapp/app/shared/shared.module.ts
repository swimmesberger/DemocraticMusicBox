import { NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { NgbDateAdapter } from '@ng-bootstrap/ng-bootstrap';

import { NgbDateMomentAdapter } from './util/datepicker-adapter';
import { DemocraticMusicBoxSharedLibsModule, DemocraticMusicBoxSharedCommonModule, HasAnyAuthorityDirective } from './';

@NgModule({
    imports: [DemocraticMusicBoxSharedLibsModule, DemocraticMusicBoxSharedCommonModule],
    declarations: [HasAnyAuthorityDirective],
    providers: [{ provide: NgbDateAdapter, useClass: NgbDateMomentAdapter }],
    exports: [DemocraticMusicBoxSharedCommonModule, HasAnyAuthorityDirective],
    schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class DemocraticMusicBoxSharedModule {}
