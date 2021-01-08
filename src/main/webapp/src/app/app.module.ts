import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { AppComponent } from './app.component';
import { SettingsComponent } from './settings/settings.component';
import { AppRoutingModule } from './app-routing.module';
import { RouterModule, Routes } from "@angular/router";
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MatGridListModule } from "@angular/material/grid-list";
import { MatCardModule } from "@angular/material/card";
import { MatButtonModule } from "@angular/material/button";
import { MatIconModule } from "@angular/material/icon";
import { MatListModule } from "@angular/material/list";
import { MatInputModule } from "@angular/material/input";
import { FormsModule } from "@angular/forms";
import { MatSelectModule } from "@angular/material/select";
import { MatCheckboxModule } from "@angular/material/checkbox";
import { FileListDialogComponent } from './file-list-dialog/file-list-dialog.component';
import { RoleManagementComponent } from './role-management/role-management.component';
import { RoleMatrixComponent } from './role-matrix/role-matrix.component';

const routes: Routes = [
  { path: 'settings', component: SettingsComponent },
  { path: 'file-list-dialog', component: FileListDialogComponent },
  { path: 'role-management', component: RoleManagementComponent },
  { path: 'role-matrix', component: RoleMatrixComponent },
];

@NgModule({
  declarations: [
    AppComponent,
    SettingsComponent,
    FileListDialogComponent,
    RoleManagementComponent,
    RoleMatrixComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    RouterModule.forRoot(routes, {useHash: true}),
    BrowserAnimationsModule,
    MatGridListModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatListModule,
    MatInputModule,
    MatSelectModule,
    MatCheckboxModule,
    FormsModule
  ],
  exports: [RouterModule],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
